// This program will compile a Traditional DAWG encoding from the "Word-List.txt" file.
// Updated on Monday, December 16, 2011.

// A graph compression algorithm this FAST is perfectly suited for record-keeping-compression while solving an NP-Complete.

// 4 Major concerns addressed:
// 1) Allowance for larger word lists. 2^25 DAWG node count is now the upper limit.
// 2) Superior "ReplaceMeWith" scheme.
// 3) The use of CRC-Digest calculation, "Tnode" segmentation, and group sorting render DAWG creation INSTANTANEOUS.
// 4) Certain Graph configurataions led the previous version of this program to crash...  NO MORE.

// "Word-List.txt" is a text file with the number of words written on the very first line, and 1 word per line after that.
// The words are case-insensitive, and the text file may have Windows or Linux format.
// *** MAX is the length of the longest word in the list. Change this value.
// *** MIN is the length of the shortest word in the list.  Change this value.
// The program DEMANDS that all 26 English letters are used, and NO additional chars can exist in the list.
// If you have novelty word lists that you need to use, then MODIFY the code, or ask me to do it for you.

// Include the big-three header files.
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

// General high-level program constants.
#define SORT_THRESHOLD 1
#define MIN 2 //default 2
#define MAX 15 //default 15
#define NUMBER_OF_ENGLISH_LETTERS 26
#define INPUT_LIMIT 35
#define LOWER_IT 32
#define TEN 10
#define INT_BITS 32
#define CHILD_BIT_SHIFT 5
#define CHILD_INDEX_BIT_MASK 0X3FFFFFE0
#define LETTER_BIT_MASK 0X0000001F
#define END_OF_WORD_BIT_MASK 0X80000000
#define END_OF_LIST_BIT_MASK 0X40000000
#define CHILD_CYPHER 0X1EDC6F41
#define NEXT_CYPHER 0X741B8CD7
#define TWO_UP_EIGHT 256
#define LEFT_BYTE_SHIFT 24
#define BYTE_WIDTH 8

// C requires a boolean variable type so use C's typedef concept to create one.
typedef enum { FALSE = 0, TRUE = 1 } Bool;
typedef Bool* BoolPtr;

// The lexicon text file.
#define RAW_LEXICON "plain_dic.txt"

// This program will create "1" binary-data file for use, and "1" text-data file for inspection.
#define TRADITIONAL_DAWG_DATA "dawg_dic.dat"
#define TRADITIONAL_DAWG_TEXT_DATA "dawg_dic.txt"

// An explicit table-lookup CRC calculation will be used to identify unique graph branch configurations.
#define LOOKUP_TABLE_DATA "CRC-32.dat"

unsigned int TheLookupTable[TWO_UP_EIGHT];

// Lookup tables used for node encoding and number-string decoding.
const int PowersOfTwo[INT_BITS] = { 0X1, 0X2, 0X4, 0X8, 0X10, 0X20, 0X40, 0X80, 0X100, 0X200, 0X400, 0X800,
 0X1000, 0X2000, 0X4000, 0X8000, 0X10000, 0X20000, 0X40000, 0X80000, 0X100000, 0X200000, 0X400000, 0X800000, 0X1000000,
 0X2000000, 0X4000000, 0X8000000, 0X10000000, 0X20000000, 0X40000000, 0X80000000 };
const int PowersOfTen[TEN] = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000 };

// This simple function clips off the extra chars for each "fgets()" line.  Works for Linux and Windows text format.
void CutOffExtraChars(char *ThisLine){
	if ( ThisLine[strlen(ThisLine) - 2] == '\r' ) ThisLine[strlen(ThisLine) - 2] = '\0';
	else if ( ThisLine[strlen(ThisLine) - 1] == '\n' ) ThisLine[strlen(ThisLine) - 1] = '\0';
}

// Returns the positive "int" rerpresented by "TheNumberNotYet" string.  An invalid "TheNumberNotYet" returns "0".
int StringToPositiveInt(char* TheNumberNotYet){
	int Result = 0;
	int X;
	int Length = strlen(TheNumberNotYet);
	if ( Length > TEN ) return 0;
	for ( X = 0; X < Length; X++ ) {
		if ( TheNumberNotYet[X] < '0' || TheNumberNotYet[X] > '9' ) return 0;
		Result += ((TheNumberNotYet[X] - '0')*PowersOfTen[Length - X - 1 ]);
	}
	return Result;
}

// The "BinaryNode" string must be at least 32 + 5 + 1 bytes in length.  Space for the bits,
// the seperation pipes, and the end of string char.
// This function is used to fill the text file used to inspect the graph created in the first segment of the program.
void ConvertIntNodeToBinaryString(int TheNode, char *BinaryNode){
	int X;
	int Bit;
	BinaryNode[0] = '[';
	// Bit 31 holds the End-Of-Word flag.
	BinaryNode[1] = (TheNode & END_OF_WORD_BIT_MASK)?'1':'0';
	BinaryNode[2] = '|';
	// Bit 30 holds the End-Of-List flag.
	BinaryNode[3] = (TheNode & END_OF_LIST_BIT_MASK)?'1':'0';
	BinaryNode[4] = '|';
	// 25 Bits, (29-->5) hold the First-Child index.
	Bit = 29;
	for ( X = 5; X <= 29; X++, Bit-- ) BinaryNode[X] = (TheNode & PowersOfTwo[Bit])?'1':'0';
	BinaryNode[30] = '|';
	// The Letter is held in the final 5 bits, (4->0).
	Bit = 4;
	for ( X = 31; X <= 35; X++, Bit-- ) BinaryNode[X] = (TheNode & PowersOfTwo[Bit])?'1':'0';
	BinaryNode[36] = ']';
	BinaryNode[37] = '\0';
}

//This Function converts any lower case letters inside "RawWord" to capitals, so that the whole string is made of capital letters.
void MakeMeAllCapital(char *RawWord){
	int Count = 0;
	int Length = strlen(RawWord);
	for ( Count = 0; Count < Length; Count++ ) {
		if ( RawWord[Count] >= 'a' && RawWord[Count] <= 'z' ) RawWord[Count] -= LOWER_IT;
	}
}

// This function performs a Byte-wise lookup table CRC calculation on "NumberOfBytes" Bytes, starting at "DataMessage".
// The Polynomial used to generate the lookup table is CRC-32 = 0X04C11DB7.
// The value returned by the function is the "CRC-Digest".
unsigned int LookupTableCrc(const unsigned char *DataMessage, int NumberOfBytes, Bool Print){
	int X;
	if ( Print ) {
		printf("|");
		for ( X = 0; X < NumberOfBytes; X++ ) {
			printf("%02X", DataMessage[X]);
			if ( X%4 == 3 ) printf("|");
		}
		printf(" - Length |%d|\n", NumberOfBytes);
	}
	// Because looking up "0" in the table returns "0", it is safe to use a table lookup to fill the "WorkingRegister" with its initial "DataMessage" value.
	register unsigned int WorkingRegister = 0;
	// Query the "LookupTable" exactly "NumberOfBytes" times.  Perform lookups using the value inside of "WorkingRegister" as the index.
	// After each table query, "XOR" the value returned by "TheLookupTable" with "WorkingRegister" after pulling in the next Byte of "DataMessage".
	// "X" is the location of the next data Byte to pull into the calculation.
	for ( X = 0; X < NumberOfBytes; X++ ) WorkingRegister = TheLookupTable[WorkingRegister >> LEFT_BYTE_SHIFT] ^ ((WorkingRegister << BYTE_WIDTH) ^ DataMessage[X]);
	if ( Print ) printf("Calculated Digest = |%08X|\n", WorkingRegister);
	return WorkingRegister;
}

/*Trie to Dawg TypeDefs*/
struct tnode {
	struct tnode* Next;
	struct tnode* Child;
	struct tnode* ParentalUnit;
	struct tnode* ReplaceMeWith;
	// When populating the DAWG array, you must know the index assigned to each "Child".
	// "ArrayIndex" Is stored in every node, so that we can mine the information from the Trie.
	int ArrayIndex;
	int InternalValues;
	char DirectChild;
	char Letter;
	char MaxChildDepth;
	char Level;
	char NumberOfChildren;
	char DistanceToEndOfList;
	char Dangling;
	char Protected;
	char EndOfWordFlag;
	unsigned int CrcDigest;
	// Assign a "NodeNumber" to each "Tnode" to identify its position in a breadth-first traversal.  This value is useful when modifying the algorithm.
	int NodeNumber;
	// To streamline checking if "Protected" "Tnode"s are up for "Dangling", filter "ProtectedUnderCount" up to the root "Tnode"; do it on the fly.
	int ProtectedUnderCount;
};

typedef struct tnode Tnode;
typedef Tnode* TnodePtr;

// Functions to access internal "Tnode" members.
int TnodeArrayIndex(TnodePtr ThisTnode){
	return ThisTnode->ArrayIndex;
}

char TnodeDirectChild(TnodePtr ThisTnode){
	return ThisTnode->DirectChild;
}

TnodePtr TnodeNext(TnodePtr ThisTnode){
	return ThisTnode->Next;
}

TnodePtr TnodeChild(TnodePtr ThisTnode){
	return ThisTnode->Child;
}

TnodePtr TnodeParentalUnit(TnodePtr ThisTnode){
	return ThisTnode->ParentalUnit;
}

TnodePtr TnodeReplaceMeWith(TnodePtr ThisTnode){
	return ThisTnode->ReplaceMeWith;
}

char TnodeLetter(TnodePtr ThisTnode){
	return ThisTnode->Letter;
}

char TnodeMaxChildDepth(TnodePtr ThisTnode){
	return ThisTnode->MaxChildDepth;
}

char TnodeNumberOfChildren(TnodePtr ThisTnode){
	return ThisTnode->NumberOfChildren;
}

char TnodeDistanceToEndOfList(TnodePtr ThisTnode){
	return ThisTnode->DistanceToEndOfList;
}

char TnodeEndOfWordFlag(TnodePtr ThisTnode){
	return ThisTnode->EndOfWordFlag;
}

char TnodeLevel(TnodePtr ThisTnode){
	return ThisTnode->Level;
}

char TnodeDangling(TnodePtr ThisTnode){
	return ThisTnode->Dangling;
}

char TnodeProtected(TnodePtr ThisTnode){
	return ThisTnode->Protected;
}

unsigned int TnodeCrcDigest(TnodePtr ThisTnode){
	return ThisTnode->CrcDigest;
}

// Allocate a "Tnode" and fill it with initial values.
TnodePtr TnodeInit(char Chap, TnodePtr OverOne, char WordEnding, char Leveler, int StarterDepth, TnodePtr Parent, char IsaChild, char StartListPosition){
	TnodePtr Result = (Tnode *)malloc(sizeof(Tnode));
	Result->Letter = Chap;
	Result->ArrayIndex = 0;
	Result->InternalValues = 0;
	Result->NumberOfChildren = 0;
	Result->DistanceToEndOfList = StartListPosition;
	Result->MaxChildDepth = StarterDepth;
	Result->Next = OverOne;
	Result->Child = NULL;
	Result->ParentalUnit = Parent;
	Result->Dangling = FALSE;
	Result->Protected = FALSE;
	Result->ReplaceMeWith = NULL;
	Result->EndOfWordFlag = WordEnding;
	Result->Level = Leveler;
	Result->DirectChild = IsaChild;
	Result->CrcDigest = 0;
	Result->NodeNumber = 0;
	Result->ProtectedUnderCount = 0;
	return Result;
}

// Use this for debugging any program modifications.
void TnodeOutput(TnodePtr ThisTnode){
	printf("|%c|%d|%d|%d|%X|-|%X|\n", ThisTnode->Letter, ThisTnode->EndOfWordFlag, ThisTnode->NumberOfChildren, ThisTnode->DistanceToEndOfList, ThisTnode->InternalValues, ThisTnode->CrcDigest);
	if ( ThisTnode->Child != NULL ) TnodeOutput(ThisTnode->Child);
}

// Modify internal "Tnode" member values.
void TnodeSetArrayIndex(TnodePtr ThisTnode, int TheWhat){
	ThisTnode->ArrayIndex = TheWhat;
}

void TnodeSetChild(TnodePtr ThisTnode, TnodePtr Chump){
	ThisTnode->Child = Chump;
}
	
void TnodeSetNext(TnodePtr ThisTnode, TnodePtr Nexis){
	ThisTnode->Next = Nexis;
}

void TnodeSetParentalUnit(TnodePtr ThisTnode, TnodePtr Parent){
	ThisTnode->ParentalUnit = Parent;
}

void TnodeSetReplaceMeWith(TnodePtr ThisTnode, TnodePtr Living){
	ThisTnode->ReplaceMeWith = Living;
}

void TnodeSetMaxChildDepth(TnodePtr ThisTnode, int NewDepth){
	ThisTnode->MaxChildDepth = NewDepth;
}

void TnodeSetDirectChild(TnodePtr ThisTnode, char Status){
	ThisTnode->DirectChild = Status;
}

void TnodeFlyEndOfWordFlag(TnodePtr ThisTnode){
	ThisTnode->EndOfWordFlag = TRUE;
}

// This statement evaluates to TRUE when the CRC at "one" has a higher value than the CRC at "two".  "one" and "two" are indicies of "arrayone", and "arraytwo".
#define COMPARE_TNODES(arrayone, one, arraytwo, two) ( (arrayone[one])->CrcDigest > (arraytwo[two])->CrcDigest )? TRUE: FALSE

void TnodeArrayMergeSortRecurse(TnodePtr *OriginalArray, int TheSize, TnodePtr *ExtraArray){
	int FirstSize = TheSize>>1;
	int SecondSize = TheSize - FirstSize;
	int FirstIndex = 0;
	int SecondIndex = 0;
	int InsertIndex = 0;
	TnodePtr *TheFirst = OriginalArray;
	TnodePtr *TheSecond = OriginalArray + FirstSize;
	// Testing the escape condition before calling "TnodeArrayMergeSort" reduces stack overhead.
	if ( FirstSize > SORT_THRESHOLD ) TnodeArrayMergeSortRecurse(TheFirst, FirstSize, ExtraArray);
	if ( SecondSize > SORT_THRESHOLD ) TnodeArrayMergeSortRecurse(TheSecond, SecondSize, ExtraArray);
	// We can now conclude that the two lists are sorted, so merge them into the "ExtraArray".
	while ( FirstIndex < FirstSize && SecondIndex < SecondSize) {
		// Using this comparison macro ensures that the sort will be stable.
		if ( COMPARE_TNODES(TheSecond, SecondIndex, TheFirst, FirstIndex) ) {
			ExtraArray[InsertIndex] = TheSecond[SecondIndex];
			SecondIndex += 1;
			InsertIndex += 1;
		}
		else {
			ExtraArray[InsertIndex] = TheFirst[FirstIndex];
			FirstIndex += 1;
			InsertIndex += 1;
		}
	}
	// This instruction copies the remaining elements from the unfinished list into the "ExtraArray".
	if ( FirstIndex == FirstSize) memcpy(ExtraArray + InsertIndex, TheSecond + SecondIndex, (SecondSize - SecondIndex)*sizeof(TnodePtr));
	else memcpy(ExtraArray + InsertIndex, TheFirst + FirstIndex, (FirstSize - FirstIndex)*sizeof(TnodePtr));
	memcpy(OriginalArray, ExtraArray, TheSize*sizeof(TnodePtr));
}

// After all words have been added to the initial Trie, this function will combine the internal comparison values of "ThisTnode" into its "InternalValues".
void TnodeCalculateInternalValues(TnodePtr ThisTnode){
	char *TheBytes = (char *)&(ThisTnode->InternalValues);
	TheBytes[0] = ThisTnode->Letter;
	TheBytes[1] = ThisTnode->NumberOfChildren;
	TheBytes[2] = ThisTnode->DistanceToEndOfList;
	TheBytes[3] = ((ThisTnode->MaxChildDepth) << 1) + ThisTnode->EndOfWordFlag;
}

// Recursively calculate all "InternalValues" within a "Tnode" graph.  "ThisTnode" must not be NULL.
void TnodeCalculateInternalValuesRecurse(TnodePtr ThisTnode){
	TnodeCalculateInternalValues(ThisTnode);
	if ( ThisTnode->Child != NULL ) TnodeCalculateInternalValuesRecurse(ThisTnode->Child);
	if ( ThisTnode->Next != NULL ) TnodeCalculateInternalValuesRecurse(ThisTnode->Next);
}

void TnodeCalculateCrcDigest(TnodePtr ThisTnode, Bool Print){
	static unsigned int TheMessage[(NUMBER_OF_ENGLISH_LETTERS + 2)<<1];
	int MessageLength;
	int FillSpace;
	int X;
	TnodePtr Current;
	if ( ThisTnode->DistanceToEndOfList == 0 ) {
		if ( ThisTnode->NumberOfChildren == 0 ) {
			ThisTnode->CrcDigest = (unsigned int)ThisTnode->InternalValues;
			return;
		}
		else {
			TheMessage[0] = (unsigned int)ThisTnode->InternalValues;
			TheMessage[1] = CHILD_CYPHER;
			MessageLength = ThisTnode->NumberOfChildren + 2;
			Current = ThisTnode->Child;
			for ( FillSpace = 2; FillSpace < MessageLength; FillSpace++ ) {
				TheMessage[FillSpace] = Current->CrcDigest;
				if ( TheMessage[FillSpace] == 0 ) printf("ZERO in CRC of Child.\n");
				Current = Current->Next;
			}
			TheMessage[MessageLength] = (unsigned int)ThisTnode->InternalValues;
			MessageLength += 1;
			
			if ( Print == TRUE ) {
				for ( X = 0; X < MessageLength; X++ ) printf("|%08X", TheMessage[X]);
				printf("|\n");
			}
			
			ThisTnode->CrcDigest = LookupTableCrc((unsigned char *)TheMessage, MessageLength<<2, Print);
			if ( Print == TRUE ) printf("Inherited  Digest = |%X| - Length|%d|\n", ThisTnode->CrcDigest, MessageLength<<2);
			return;
		}
	}
	if ( ThisTnode->NumberOfChildren == 0 ) {
		TheMessage[0] = (unsigned int)ThisTnode->InternalValues;
		TheMessage[1] = NEXT_CYPHER;
		MessageLength = ThisTnode->DistanceToEndOfList + 2;
		Current = ThisTnode->Next;
		for ( FillSpace = 2; FillSpace < MessageLength; FillSpace++ ) {
			TheMessage[FillSpace] = Current->CrcDigest;
			if ( TheMessage[FillSpace] == 0 ) printf("ZERO in CRC of Next.\n");
			Current = Current->Next;
		}
		
		TheMessage[MessageLength] = (unsigned int)ThisTnode->InternalValues;
		MessageLength += 1;
		
		ThisTnode->CrcDigest = LookupTableCrc((unsigned char *)TheMessage, MessageLength<<2, Print);
		return;
	}
	TheMessage[0] = (unsigned int)ThisTnode->InternalValues;
	TheMessage[1] = CHILD_CYPHER;
	MessageLength = ThisTnode->NumberOfChildren + 2;
	Current = ThisTnode->Child;
	for ( FillSpace = 2; FillSpace < MessageLength; FillSpace++ ) {
		TheMessage[FillSpace] = Current->CrcDigest;
		if ( TheMessage[FillSpace] == 0 ) printf("ZERO in CRC of BChild.\n");
		Current = Current->Next;
	}
	TheMessage[MessageLength] = NEXT_CYPHER;
	MessageLength += ThisTnode->DistanceToEndOfList + 1;
	Current = ThisTnode->Next;
	for ( FillSpace += 1; FillSpace < MessageLength; FillSpace++ ) {
		TheMessage[FillSpace] = Current->CrcDigest;
		if ( TheMessage[FillSpace] == 0 ) printf("ZERO in CRC of BNext.\n");
		Current = Current->Next;
	}
	
	TheMessage[MessageLength] = (unsigned int)ThisTnode->InternalValues;
	MessageLength += 1;
	
	ThisTnode->CrcDigest = LookupTableCrc((unsigned char *)TheMessage, MessageLength<<2, Print);
	return;
}

void TnodeCalculateCrcDigestRecurse(TnodePtr ThisTnode){
	if ( ThisTnode->Next != NULL ) TnodeCalculateCrcDigestRecurse(ThisTnode->Next);
	if ( ThisTnode->Child != NULL ) TnodeCalculateCrcDigestRecurse(ThisTnode->Child);
	TnodeCalculateCrcDigest(ThisTnode, FALSE);
}

// This function Dangles a "Tnode", but also recursively dangles every "Tnode" after and under it as well.
// Dangling a "Tnode" means that it will be exculded from the final "DAWG" encoding.
// By recursion, nodes that are not direct children will get dangled.
// The function returns the total number of nodes dangled as a result.
int TnodeDangleRecurse(TnodePtr ThisTnode){
	int Result = 0;
	if ( ThisTnode->Dangling == TRUE ) return 0;
	if ( ThisTnode->Protected == TRUE ) {
		printf("  There is NO scenario where Dangling a Protected node should happen.  ERROR, ERROR, ERROR.\n");
		return 0;
	}
	if ( (ThisTnode->Next) != NULL ) Result += TnodeDangleRecurse(ThisTnode->Next);
	if ( (ThisTnode->Child) != NULL ) Result += TnodeDangleRecurse(ThisTnode->Child);
	if ( ThisTnode->Dangling == FALSE )Result += 1;
	ThisTnode->Dangling = TRUE;
	return Result;
}

// This function "Protects" a node being directly referenced in the elimination process.
// "Protected" "Tnode"s should NEVER be "Dangled".
// Make sure to increment "ProtectedUnderCount" by "1" all the way up to the root "Tnode".
void TnodeProtect(TnodePtr ThisTnode){
	TnodePtr Current = ThisTnode;
	if ( ThisTnode->Protected == FALSE ) {
		ThisTnode->Protected = TRUE;
		while ( Current != NULL ) {
			Current->ProtectedUnderCount += 1;
			Current = Current->ParentalUnit;
		}
	}
}

// This function returns the pointer to the "Tnode" in a parallel list of "Tnodes" with the letter "ThisLetter",
// and returns "NULL" if the "Tnode" does not exist.
// If the function returns "NULL", then an insertion is required.
TnodePtr TnodeFindParaNode(TnodePtr ThisTnode, char ThisLetter){
	TnodePtr Result = ThisTnode;
	if ( ThisTnode == NULL ) return NULL;
	if ( Result->Letter == ThisLetter ) return Result;
	while ( Result->Letter < ThisLetter ) {
		Result = Result->Next;
		if ( Result == NULL ) return NULL;
	}
	if ( Result->Letter == ThisLetter ) return Result;
	else return NULL;
}

// This function inserts a new "Tnode" before a larger letter "Tnode" or at the end of a para list.
// If the list does not exist, then it is put at the beginnung.  
// The new "Tnode" has "ThisLetter" in it.  "AboveTnode" is the "Tnode" 1 level above where the new node will be placed.
// This function should never be passed a "NULL" pointer.  "ThisLetter" should never exist in the "Child" para-list.
void TnodeInsertParaNode(TnodePtr AboveTnode, char ThisLetter, char WordEnder, int StartDepth){
	AboveTnode->NumberOfChildren += 1;
	TnodePtr Holder = NULL;
	TnodePtr Currently = NULL;
	// Case 1: ParaList does not exist yet so start it.
	if ( AboveTnode->Child == NULL ) AboveTnode->Child = TnodeInit(ThisLetter, NULL, WordEnder, AboveTnode->Level + 1,
	StartDepth, AboveTnode, TRUE, 0);
	// Case 2: ThisLetter should be the first in the ParaList.
	else if ( ((AboveTnode->Child)->Letter) > ThisLetter ) {
		Holder = AboveTnode->Child;
		// The holder node is no longer a direct child so set it as such.
		TnodeSetDirectChild(Holder, FALSE);
		AboveTnode->Child = TnodeInit(ThisLetter, Holder, WordEnder, AboveTnode->Level + 1, StartDepth, AboveTnode, TRUE, TnodeDistanceToEndOfList(Holder) + 1);
		// The parent node needs to be changed on what used to be the child. it is the Tnode in "Holder".
		Holder->ParentalUnit = AboveTnode->Child;
	}
	// Case 3: The ParaList exists and ThisLetter is not first in the list.
	else {
		Currently = AboveTnode->Child;
		while ( Currently->Next !=NULL ) {
			if ( TnodeLetter(Currently->Next) > ThisLetter ) break;
			Currently->DistanceToEndOfList += 1;
			Currently = Currently->Next;
		}
		Holder = Currently->Next;
		Currently->Next = TnodeInit(ThisLetter, Holder, WordEnder, AboveTnode->Level + 1, StartDepth, Currently, FALSE, Currently->DistanceToEndOfList);
		Currently->DistanceToEndOfList += 1;
		if ( Holder != NULL ) Holder->ParentalUnit = Currently->Next;
	}
}

// This function returns "TRUE" if "FirstNode" and "SecondNode" are the same inside, below, and after, and for now it is recursive.
// All of the internal comparison values will be combined into one 4 Byte integer, "InternalValues".
// For now, due only to recursion, we must still screen for NULL pointers.
char TnodeAreWeTheSame(TnodePtr FirstNode, TnodePtr SecondNode){
	if ( FirstNode == SecondNode ) return TRUE;
	if ( FirstNode == NULL || SecondNode == NULL ) return FALSE;
	if ( FirstNode->InternalValues != SecondNode->InternalValues ) return FALSE;
	if ( TnodeAreWeTheSame(FirstNode->Child, SecondNode->Child) == FALSE ) return FALSE;
	if ( TnodeAreWeTheSame(FirstNode->Next, SecondNode->Next) == FALSE ) return FALSE;
	else return TRUE;
}

struct dawg {
	int NumberOfTotalWords;
	int NumberOfTotalNodes;
	TnodePtr First;
};

typedef struct dawg Dawg;
typedef Dawg* DawgPtr;

// Set up the parent nodes in the Dawg.
DawgPtr DawgInit(void){
	DawgPtr Result = (Dawg *)malloc(sizeof(Dawg));
	Result->NumberOfTotalWords = 0;
	Result->NumberOfTotalNodes = 0;
	Result->First = TnodeInit('0', NULL, FALSE, 0, 0, NULL, FALSE, 0);
	return Result;
}

// Return the root node of "ThisDawg", which is a direct child of the "First" node.
TnodePtr DawgRootNode(DawgPtr ThisDawg){
	return TnodeChild(ThisDawg->First);
}

// This function is responsible for adding "Word" to the "Dawg" under its root node.
// It returns the number of new nodes inserted.
int TnodeDawgAddWord(TnodePtr ParentNode, const char *Word){
	int Result = 0;
	int X, Y = 0;
	int WordLength = strlen(Word);
	TnodePtr HangPoint = NULL;
	TnodePtr Current = ParentNode;
	for ( X = 0; X < WordLength; X++){
		HangPoint = TnodeFindParaNode(TnodeChild(Current), Word[X]);
		if ( HangPoint == NULL ) {
			TnodeInsertParaNode(Current, Word[X], (X == WordLength - 1 ? TRUE : FALSE), WordLength - X - 1);
			Result++;
			Current = TnodeFindParaNode(TnodeChild(Current), Word[X]);
			for ( Y = X + 1; Y < WordLength; Y++ ) {
				TnodeInsertParaNode(Current, Word[Y], (Y == WordLength - 1 ? TRUE : FALSE), WordLength - Y - 1);
				Result += 1;
				Current = TnodeChild(Current);
			}
			break;
		}
		else {
			if ( TnodeMaxChildDepth(HangPoint) < WordLength - X - 1 ) TnodeSetMaxChildDepth(HangPoint, WordLength - X - 1);
		}
		Current = HangPoint;
		// The path for the word that we are trying to insert already exists,
		// so just make sure that the end flag is flying on the last node.
		// This should never happen if we are to add words in alphabetical order and increasing word length.
		if ( X == WordLength - 1 ) TnodeFlyEndOfWordFlag(Current);
	}
	return Result;
}

// Add "NewWord" to "ThisDawg", which at this point is a "Trie" with a lot of information in each node.
// "NewWord" must not exist in "ThisDawg" already.
void DawgAddWord(DawgPtr ThisDawg, char * NewWord){
	ThisDawg->NumberOfTotalWords += 1;
	int NodesAdded = TnodeDawgAddWord(ThisDawg->First, NewWord);
	ThisDawg->NumberOfTotalNodes += NodesAdded;
}

// This is a standard depth first preorder tree traversal.
// Count un"Dangling" "Tnodes" into the 780 groups by "MaxChildDepth", "Letter", and "DirectChild", then store values into "Tabulator".
void TnodeGraphTabulateRecurse(TnodePtr ThisTnode, int*** Tabulator){
	// We will only ever be concerned with "Living" nodes.  "Dangling" Nodes will be eliminated, so don't count them.
	if ( ThisTnode->Dangling == FALSE ) {
		Tabulator[ThisTnode->MaxChildDepth][ThisTnode->Letter - 'A'][ThisTnode->DirectChild] += 1;
		// Go Down if possible.
		if ( ThisTnode->Child != NULL ) TnodeGraphTabulateRecurse(TnodeChild(ThisTnode), Tabulator);
		// Go Right if possible.
		if ( ThisTnode->Next != NULL ) TnodeGraphTabulateRecurse(TnodeNext(ThisTnode), Tabulator);
	}
}

// Count the "Living" "Tnode"s of each "MaxChildDepth" in "ThisDawg", and store the values in "Count".
void DawgGraphTabulate(DawgPtr ThisDawg, int*** Count){
	if ( ThisDawg->NumberOfTotalWords > 0 ) {
		TnodeGraphTabulateRecurse(TnodeChild(ThisDawg->First), Count);
	}
}

// Recursively replaces all redundant nodes under "ThisTnode".
// "DirectChild" "Tnode"s in a "Dangling" state have "ReplaceMeWith" set within them.
void TnodeBlitzAttackRecurse(TnodePtr ThisTnode){
	if ( ThisTnode->Next == NULL && ThisTnode->Child == NULL ) return;
	// The first "Tnode" being eliminated will always be a "DirectChild".
	if ( ThisTnode->Child != NULL ) {
		// The node is tagged to be excised, so replace it with "ReplaceMeWith".
		if ( (ThisTnode->Child)->Dangling == TRUE ) {
			ThisTnode->Child = TnodeReplaceMeWith(ThisTnode->Child);
		}
		else {
			TnodeBlitzAttackRecurse(ThisTnode->Child);
		}
	}
	if ( ThisTnode->Next != NULL ){
		TnodeBlitzAttackRecurse(ThisTnode->Next);
	}
}

// Replaces all pointers to "Dangling" "Tnodes" in the "ThisDawg" Trie with living ones.
void BlitzkriegTrieAttack(DawgPtr ThisDawg){
	TnodeBlitzAttackRecurse(ThisDawg->First->Child);
}

// A recursive function which Exchanges a single "Protected" "Tnode" under "ToDangle" with the corresponding "Tnode" under "ToKeep".
// Remember to update "ProtectedUnderCount" for each line of "Tnodes" after the exchange.
void TnodeExchangeProtectedNodeRecurse(TnodePtr ToDangle, TnodePtr ToKeep){
	int ProtectedUnderCountParity;
	TnodePtr Holder;
	if ( ToDangle->Protected == TRUE) {
		if ( ToDangle->DirectChild == TRUE ) {
			//printf("Protected ToDangle = DirectChild");
			if ( ToKeep->ReplaceMeWith == ToDangle ) {
				//printf(" - Standard Crosslink");
				ProtectedUnderCountParity = ToDangle->ProtectedUnderCount - ToKeep->ProtectedUnderCount;
				Holder = ToDangle->ParentalUnit;
				while ( Holder != NULL ) {
					Holder->ProtectedUnderCount -= ProtectedUnderCountParity;
					Holder = Holder->ParentalUnit;
				}
				Holder = ToKeep->ParentalUnit;
				while ( Holder != NULL ) {
					Holder->ProtectedUnderCount += ProtectedUnderCountParity;
					Holder = Holder->ParentalUnit;
				}
				(ToKeep->ParentalUnit)->Child = ToDangle;
				(ToDangle->ParentalUnit)->Child = ToKeep;
				Holder = ToKeep->ParentalUnit;
				ToKeep->ParentalUnit = ToDangle->ParentalUnit;
				ToDangle->ParentalUnit = Holder;
				return;
			}
			else {
				//printf(" - No Crosslink");
				//if ( ToKeep->Dangling == FALSE) printf(" - ToKeep != Dangling - IMPOSSIBLE");
				//if ( ToKeep->ReplaceMeWith == NULL ) printf(" - ReplaceMeWith = NULL");
				//else printf(" - ReplaceMeWith != NULL");
				return;
			}
		}
		else {
			//printf("Protected ToDangle != DirectChild");
			if ( ToKeep->Dangling == TRUE) {
				//printf(" - ToKeep = Dangling - Something is FUCKED up.");
				return;
			}
			else {
				//printf(" - ToKeep != Dangling");
				ProtectedUnderCountParity = ToDangle->ProtectedUnderCount - ToKeep->ProtectedUnderCount;
				Holder = ToDangle->ParentalUnit;
				while ( Holder != NULL ) {
					Holder->ProtectedUnderCount -= ProtectedUnderCountParity;
					Holder = Holder->ParentalUnit;
				}
				Holder = ToKeep->ParentalUnit;
				while ( Holder != NULL ) {
					Holder->ProtectedUnderCount += ProtectedUnderCountParity;
					Holder = Holder->ParentalUnit;
				}
				(ToKeep->ParentalUnit)->Next = ToDangle;
				(ToDangle->ParentalUnit)->Next = ToKeep;
				Holder = ToKeep->ParentalUnit;
				ToKeep->ParentalUnit = ToDangle->ParentalUnit;
				ToDangle->ParentalUnit = Holder;
				return;
			}
		}
	}
	if ( ToDangle->Child != NULL ) TnodeExchangeProtectedNodeRecurse(ToDangle->Child, ToKeep->Child);
	if ( ToDangle->Next != NULL ) TnodeExchangeProtectedNodeRecurse(ToDangle->Next, ToKeep->Next);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// A queue is required for breadth first traversal, and the rest is self-evident.

struct breadthqueuenode {
	TnodePtr Element;
	struct breadthqueuenode *Next;
};

typedef struct breadthqueuenode BreadthQueueNode;
typedef BreadthQueueNode* BreadthQueueNodePtr;

void BreadthQueueNodeSetNext(BreadthQueueNodePtr ThisBreadthQueueNode, BreadthQueueNodePtr Nexit){
		ThisBreadthQueueNode->Next = Nexit;
}

BreadthQueueNodePtr BreadthQueueNodeNext(BreadthQueueNodePtr ThisBreadthQueueNode){
		return ThisBreadthQueueNode->Next;
}

TnodePtr BreadthQueueNodeElement(BreadthQueueNodePtr ThisBreadthQueueNode){
		return ThisBreadthQueueNode->Element;
}

BreadthQueueNodePtr BreadthQueueNodeInit(TnodePtr NewElement){
	BreadthQueueNodePtr Result = (BreadthQueueNode *)malloc(sizeof(BreadthQueueNode));
	Result->Element = NewElement;
	Result->Next = NULL;
	return Result;
}

struct breadthqueue {
	BreadthQueueNodePtr Front;
	BreadthQueueNodePtr Back;
	int Size;
};

typedef struct breadthqueue BreadthQueue;
typedef BreadthQueue* BreadthQueuePtr;

BreadthQueuePtr BreadthQueueInit(void){
	BreadthQueuePtr Result = (BreadthQueue *)malloc(sizeof(BreadthQueue));
	Result->Front = NULL;
	Result->Back = NULL;
	Result->Size = 0;
	return Result;
}

void BreadthQueuePush(BreadthQueuePtr ThisBreadthQueue, TnodePtr NewElemental){
	BreadthQueueNodePtr Noob = BreadthQueueNodeInit(NewElemental);
	if ( (ThisBreadthQueue->Back) != NULL ) BreadthQueueNodeSetNext(ThisBreadthQueue->Back, Noob);
	else ThisBreadthQueue->Front = Noob;
	ThisBreadthQueue->Back = Noob;
	(ThisBreadthQueue->Size) += 1;
}

TnodePtr BreadthQueuePop(BreadthQueuePtr ThisBreadthQueue){
	if ( ThisBreadthQueue->Size == 0 ) return NULL;
	if ( ThisBreadthQueue->Size == 1 ) {
		ThisBreadthQueue->Back = NULL;
		ThisBreadthQueue->Size = 0;
		TnodePtr Result = (ThisBreadthQueue->Front)->Element;
		free(ThisBreadthQueue->Front);
		ThisBreadthQueue->Front = NULL;
		return Result;
	}
	TnodePtr Result = (ThisBreadthQueue->Front)->Element;
	BreadthQueueNodePtr Holder = ThisBreadthQueue->Front;
	ThisBreadthQueue->Front = (ThisBreadthQueue->Front)->Next;
	free(Holder);
	ThisBreadthQueue->Size -= 1;
	return Result;
}


// For the "Tnode" "Dangling" process, arrange the "Tnodes" in the "Holder" array, with breadth-first traversal order.
void BreadthQueuePopulateReductionArray(BreadthQueuePtr ThisBreadthQueue, TnodePtr Root, TnodePtr ****Holder){
	int InsertionPosition[MAX][NUMBER_OF_ENGLISH_LETTERS][2];
	int CurrentNodeNumber = 0;
	int CMCD;
	char CLetter;
	char CDCstatus;
	memset(InsertionPosition, 0, MAX*NUMBER_OF_ENGLISH_LETTERS*2*sizeof(int));
	TnodePtr Current = Root;
	// Push the first row onto the queue.
	while ( Current != NULL ) {
		BreadthQueuePush(ThisBreadthQueue, Current);
		Current = Current->Next;
	}
	// Initiate the pop followed by push all children loop.
	while ( (ThisBreadthQueue->Size) != 0 ) {
		CurrentNodeNumber += 1;
		Current = BreadthQueuePop(ThisBreadthQueue);
		CMCD = Current->MaxChildDepth;
		CLetter = Current->Letter - 'A';
		CDCstatus = Current->DirectChild;
		Current->NodeNumber = CurrentNodeNumber;
		Holder[CMCD][CLetter][CDCstatus][InsertionPosition[CMCD][CLetter][CDCstatus]] = Current;
		InsertionPosition[CMCD][CLetter][CDCstatus] += 1;
		Current = TnodeChild(Current);
		while ( Current != NULL ) {
			BreadthQueuePush(ThisBreadthQueue, Current);
			Current = TnodeNext(Current);
		}
	}
	printf("\n  Final breadth queue NodeNumber = |%d|\n", CurrentNodeNumber);
}


// It is of absolutely critical importance that only "DirectChild" nodes are pushed onto the queue as child nodes.
// This will not always be the case.
// In a DAWG a child pointer may point to an internal node in a longer list.  Check for this.
int BreadthQueueUseToIndex(BreadthQueuePtr ThisBreadthQueue, TnodePtr Root){
	int IndexNow = 0;
	TnodePtr Current = Root;
	// Push the first row onto the queue.
	while ( Current != NULL ) {
		BreadthQueuePush(ThisBreadthQueue, Current);
		Current = Current->Next;
	}
	// Pop each element off of the queue and only push its children if has not been "Dangled" yet.
	// Assign index if one has not been given to it yet.
	while ( (ThisBreadthQueue->Size) != 0 ) {
		Current = BreadthQueuePop(ThisBreadthQueue);
		// A traversal of the Trie will never land on "Dangling" "Tnodes", but it will try to visit certain "Tnodes" many times.
		if ( TnodeArrayIndex(Current) == 0 ) {
			IndexNow += 1;
			TnodeSetArrayIndex(Current, IndexNow);
			Current = TnodeChild(Current);
			if ( Current != NULL ) {
				// The graph will lead to intermediate positions, but we cannot start numbering "Tnodes" from the middle of a list.
				if ( TnodeDirectChild(Current) == TRUE && TnodeArrayIndex(Current) == 0 ) {
					while ( Current != NULL ) {
						BreadthQueuePush(ThisBreadthQueue, Current);
						Current = Current->Next;
					}
				}
			}
		}
	}
	return IndexNow;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// Next and Child become indices.
struct arraydnode{
	int Next;
	int Child;
	char Letter;
	char EndOfWordFlag;
	char Level;
	char Position;
};

typedef struct arraydnode ArrayDnode;
typedef ArrayDnode* ArrayDnodePtr;

void ArrayDnodeInit(ArrayDnodePtr ThisArrayDnode, char Chap, int Nextt, int Childd, char EndingFlag, char Breadth, char Posit){
	ThisArrayDnode->Letter = Chap;
	ThisArrayDnode->EndOfWordFlag = EndingFlag;
	ThisArrayDnode->Next = Nextt;
	ThisArrayDnode->Child = Childd;
	ThisArrayDnode->Level = Breadth;
	ThisArrayDnode->Position = Posit;
}

void ArrayDnodeTnodeTranspose(ArrayDnodePtr ThisArrayDnode, TnodePtr ThisTnode){
	ThisArrayDnode->Letter = ThisTnode->Letter;
	ThisArrayDnode->EndOfWordFlag = ThisTnode->EndOfWordFlag;
	ThisArrayDnode->Level = ThisTnode->Level;
	ThisArrayDnode->Position = ThisTnode->DistanceToEndOfList;
	if ( ThisTnode->Next == NULL ) ThisArrayDnode->Next = 0;
	else ThisArrayDnode->Next = (ThisTnode->Next)->ArrayIndex;
	if ( ThisTnode->Child == NULL ) ThisArrayDnode->Child = 0;
	else ThisArrayDnode->Child = (ThisTnode->Child)->ArrayIndex;
}

int ArrayDnodeNext(ArrayDnodePtr ThisArrayDnode){
	return ThisArrayDnode->Next;
}

int ArrayDnodeChild (ArrayDnodePtr ThisArrayDnode){
	return ThisArrayDnode->Child;
}

char ArrayDnodeLetter(ArrayDnodePtr ThisArrayDnode){
	return ThisArrayDnode->Letter;
}

char ArrayDnodeEndOfWordFlag(ArrayDnodePtr ThisArrayDnode){
	return ThisArrayDnode->EndOfWordFlag;
}

struct arraydawg {
	int NumberOfStrings;
	ArrayDnodePtr DawgArray;
	int First;
	char MinStringLength;
};

typedef struct arraydawg ArrayDawg;
typedef ArrayDawg* ArrayDawgPtr;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// This function is the core of the DAWG creation procedure.  Pay close attention to the order of the steps involved.

ArrayDawgPtr ArrayDawgInit(char **Dictionary, int *SegmentLenghts){
	int X;
	int Y;
	int Z;
	int W;
	
	printf("Step 0 - Allocate the framework for the intermediate Array-Data-Structure.\n");
	// Dynamically allocate the upper Data-Structure.
	ArrayDawgPtr Result = (ArrayDawgPtr)malloc(sizeof(ArrayDawg));
	// set MinStringLength, and NumberOfStrings.
	Result->MinStringLength = MIN;
	Result->NumberOfStrings = 0;
	for ( X = Result->MinStringLength; X <= MAX ; X++ ) Result->NumberOfStrings += SegmentLenghts[X];

	printf("\nStep 1 - Create a TemporaryTrie and begin filling it with the |%d| words.\n", Result->NumberOfStrings);
	/// Create a Temp Trie structure and then feed in the given dictionary.
	DawgPtr TemporaryTrie = DawgInit();
	for ( Y = Result->MinStringLength; Y <= MAX; Y++ ) {
		for ( X = 0; X < SegmentLenghts[Y]; X++ ) {
			DawgAddWord(TemporaryTrie, &(Dictionary[Y][(Y + 1)*X]));
		}
	}

	printf("\nStep 2 - Finished filling TemporaryTrie, so calculate the InternalValues comparison integers.\n");
	
	TnodeCalculateInternalValuesRecurse(DawgRootNode(TemporaryTrie));
	
	printf("\nStep 3 - Eliminate recursion by calculating the recursive CrcDigest for each Tnode.\n");
	
	TnodeCalculateCrcDigestRecurse(DawgRootNode(TemporaryTrie));
	
	printf("\nStep 4 - Count Tnodes into 780 groups, segmented by MaxChildDepth, Letter, and DirectChild.\n");
	
	// Allocate 3D arrays of "int"s to count the "Tnodes" into groups.
	int ***NodeGroupCounter= (int ***)malloc(MAX*sizeof(int **));
	int ***NodeGroupCounterInit = (int ***)malloc(MAX*sizeof(int **));
	
	for ( X = 0; X < MAX; X++ ) {
		NodeGroupCounterInit[X] = (int **)malloc(NUMBER_OF_ENGLISH_LETTERS*sizeof(int *));
		NodeGroupCounter[X] = (int **)malloc(NUMBER_OF_ENGLISH_LETTERS*sizeof(int *));
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			NodeGroupCounterInit[X][Y] = (int *)calloc(2, sizeof(int));
			NodeGroupCounter[X][Y] = (int *)calloc(2, sizeof(int));
		}
	}
	
	DawgGraphTabulate(TemporaryTrie, NodeGroupCounterInit);
	
	printf("\nStep 5 - Initial Tnode counting is complete, so display results:\n");
	int TotalNodeSum = 0;
	int MaxGroupSize = 0;
	int CurrentGroupSize;
	for ( X = 0; X < MAX; X++ ) {
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			CurrentGroupSize = NodeGroupCounterInit[X][Y][0] + NodeGroupCounterInit[X][Y][1];
			TotalNodeSum += CurrentGroupSize;
			if ( CurrentGroupSize > MaxGroupSize ) MaxGroupSize = CurrentGroupSize;
		}
	}
	//for ( X = MAX - 1; X >= 0; X-- ) {
	//	printf("--------------------\nMaxChildDepth = |%d|\n--------------------\n", X);
	//	for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
	//		printf("|%c| = Mid|%5d| - DC|%5d|\n", Y + 'A', NodeGroupCounterInit[X][Y][0], NodeGroupCounterInit[X][Y][1]);
	//	}
	//}
	printf("\n  Total Tnode Count For The Raw-Trie = |%d|, MaxGroupSize = |%d| \n", TotalNodeSum, MaxGroupSize);
	// We will have exactly enough space for all of the Tnode pointers.

	printf("\nStep 6 - Allocate a 4-D array of Tnode pointers to tag redundant Tnodes for replacement.\n");
	
	TnodePtr ****HolderOfAllTnodePointers = (TnodePtr ****)malloc(MAX*sizeof(TnodePtr ***));
	for ( X = 0; X < MAX; X++ ) {
		HolderOfAllTnodePointers[X] = (TnodePtr ***)malloc(NUMBER_OF_ENGLISH_LETTERS*sizeof(TnodePtr **));
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			HolderOfAllTnodePointers[X][Y] = (TnodePtr **)malloc(3*sizeof(TnodePtr *));
			CurrentGroupSize = NodeGroupCounterInit[X][Y][0] + NodeGroupCounterInit[X][Y][1];
			if ( CurrentGroupSize ) {
				HolderOfAllTnodePointers[X][Y][2] = (TnodePtr *)malloc(CurrentGroupSize*sizeof(TnodePtr));
				if ( NodeGroupCounterInit[X][Y][0] ) HolderOfAllTnodePointers[X][Y][0] = HolderOfAllTnodePointers[X][Y][2];
				else HolderOfAllTnodePointers[X][Y][0] = NULL;
				if ( NodeGroupCounterInit[X][Y][1] ) {
					HolderOfAllTnodePointers[X][Y][1] = HolderOfAllTnodePointers[X][Y][2] + NodeGroupCounterInit[X][Y][0];
				}
				else HolderOfAllTnodePointers[X][Y][1] = NULL;
			}
			else {
				HolderOfAllTnodePointers[X][Y][0] = NULL;
				HolderOfAllTnodePointers[X][Y][1] = NULL;
				HolderOfAllTnodePointers[X][Y][2] = NULL;
				
			}
		}
	}
	
	// A breadth-first traversal is used when populating the final array.
	// It is then much more likely for living "Tnode"s to appear first, if we fill "HolderOfAllTnodePointers" breadth first.

	printf("\nStep 7 - Populate the 4 dimensional Tnode pointer array, keeping DirectChild nodes closer to the end.\n");
	// Use a breadth first traversal to populate the "HolderOfAllTnodePointers" array.
	BreadthQueuePtr Populator = BreadthQueueInit();
	BreadthQueuePopulateReductionArray(Populator, DawgRootNode(TemporaryTrie), HolderOfAllTnodePointers);
	free(Populator);

	// "HolderOfAllTnodePointers" Population procedure is complete.
	
	printf("\nStep 8 - Use the stable Merge-Sort algorithm to sort [MaxChildDepth][Letter] groups by CrcDigest values.\n");
	
	TnodePtr *SupplementalArray = (TnodePtr *)malloc(MaxGroupSize*sizeof(TnodePtr));
	
	for ( X = 0; X < MAX; X++ ) {
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			TnodeArrayMergeSortRecurse(HolderOfAllTnodePointers[X][Y][2], (NodeGroupCounterInit[X][Y][0] + NodeGroupCounterInit[X][Y][1]), SupplementalArray);
		}
	}
	
	// Flag all of the reduntant "Tnode"s, and store a "ReplaceMeWith" "Tnode" reference inside the "Dangling" "Tnode"s.
	// "Tnode"s are compared using their "CrcDigest" values, which incorporate information from entire branch structures.
	int NumberDangled;
	int DangledNow;
	int DirectDangled;
	int TotalDangled = 0;
	unsigned int CurrentCrcDigest;
	TnodePtr CorrectReplacementTnode;
	
	printf("\nStep 9 - Tag entire Tnode branch structures as Dangling - Elimination begins with DirectChild Tnodes and filters down:\n");
	printf("\n  This procedure is at the very heart of DAWG genesis, where the Blitzkrieg Algorithm shines with CRC, and Tnode Segmentation.\n");
	printf("  Groups of Tnodes, segmented by [MaxChildDepth] and [Letter] appear ordered by [NodeNumber] and [DirectChild], then sorted by CrcDigest.\n");
	printf("  This Blitzkrieg Scheme means that all redundant Tnode patches will be directly adjacent to their living Tnode replacement.\n");
	printf("\n  ---------------------------------------------------------------------------------------------------------------------------\n");
	// "X" is the current "MaxChildDepth".
	for ( X = MAX - 1; X >= 0; X-- ) {
		NumberDangled = 0;
		DirectDangled = 0;
		// "Y" is the current "Letter", starting at "0".
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			CurrentGroupSize = NodeGroupCounterInit[X][Y][0] + NodeGroupCounterInit[X][Y][1];
			CorrectReplacementTnode = NULL;
			CurrentCrcDigest = 0;
			// "Z" Will move through the current "Tnode" group, identifying the "CorrectReplacementTnode".
			for ( Z = 0; Z < CurrentGroupSize; Z++ ) {
				if ( HolderOfAllTnodePointers[X][Y][2][Z]->Dangling ) continue;
				CorrectReplacementTnode = HolderOfAllTnodePointers[X][Y][2][Z];
				CurrentCrcDigest = CorrectReplacementTnode->CrcDigest;
				// "W" Tracks the "Tnodes" that will be Dangled, and shifts "Z" when it finds a new "CrcDigest".
				for ( W = Z + 1; W < CurrentGroupSize; W++ ) {
					if ( HolderOfAllTnodePointers[X][Y][2][W]->CrcDigest == CurrentCrcDigest) {
						if ( HolderOfAllTnodePointers[X][Y][2][W]->Dangling ) continue;
						if ( HolderOfAllTnodePointers[X][Y][2][W]->DirectChild == FALSE ) continue;
						// If the potential replacement "Tnode" has "Protected" "Tnode"s under it, then proceed to exchange the offending branch.
						if ( HolderOfAllTnodePointers[X][Y][2][W]->ProtectedUnderCount ) {
							//printf("  Attempting to Dangle Protected, Count = |%d|", HolderOfAllTnodePointers[X][Y][2][W]->ProtectedUnderCount);
							TnodeExchangeProtectedNodeRecurse(HolderOfAllTnodePointers[X][Y][2][W], CorrectReplacementTnode);
							//printf(", after swap Count = |%d|.\n", HolderOfAllTnodePointers[X][Y][2][W]->ProtectedUnderCount);
						}
						HolderOfAllTnodePointers[X][Y][2][W]->ReplaceMeWith = CorrectReplacementTnode;
						TnodeProtect(CorrectReplacementTnode);
						DirectDangled += 1;
						DangledNow = TnodeDangleRecurse(HolderOfAllTnodePointers[X][Y][2][W]);
						NumberDangled += DangledNow;
					}
					else {
						Z = W - 1;
						break;
					}
				}
			}
		}
		printf("  DirectDangled |%5d| Tnodes, and |%5d| through recursion - MCD|%2d|\n", DirectDangled, NumberDangled, X);
		TotalDangled += NumberDangled;
	}
	printf("  ---------------------------------------------------------------------------------------------------------------------------\n\n");
	
	int NumberOfLivingNodes;
	printf("  |%6d| = Original # of Tnodes.\n", TotalNodeSum);	
	printf("  |%6d| = Dangled # of Tnodes.\n", TotalDangled);
	printf("  |%6d| = Remaining # of Tnodes.\n", NumberOfLivingNodes = TotalNodeSum - TotalDangled);

	printf("\nStep 10 - Count the number of living Tnodes by traversing the Raw-Trie to check the Dangling numbers.\n\n");
	DawgGraphTabulate(TemporaryTrie, NodeGroupCounter);
	int TotalDangledCheck = 0;
	for ( X = 0; X < MAX; X++ ) {
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			for ( Z = 0; Z < 2; Z++ ) {
				TotalDangledCheck += (NodeGroupCounterInit[X][Y][Z] - NodeGroupCounter[X][Y][Z]);
			}
		}
	}
	if ( TotalDangled == TotalDangledCheck ) printf("  Tnode Dangling count is consistent.\n");
	else printf("  MISMATCH for Tnode Dangling count.\n");
	
	printf("\nstep 11 - Using the BlitzkriegTrieAttack, substitute Dangling Tnodes with internal \"ReplaceMeWith\" values.\n");
	// Node replacement has to take place before indices are set up so nothing points to redundant nodes.
	// - This step is absolutely critical.  Mow The Lawn so to speak!  Then Index.
	BlitzkriegTrieAttack(TemporaryTrie);
	printf("\n  Killing complete.\n");

	printf("\nStep 12 - Blitzkrieg Attack is victorious, so assign array indicies to all living Tnodes using a Breadth-First-Queue.\n");
	BreadthQueuePtr OrderMatters = BreadthQueueInit();
	// The Breadth-First-Queue must assign an index value to each living "Tnode" only once.
	// Make sure to feed the root Tnode of "TemporaryTrie" into the "BreadthQueueUseToIndex()" function.
	int IndexCount = BreadthQueueUseToIndex(OrderMatters, DawgRootNode(TemporaryTrie));
	free(OrderMatters);
	printf("\n  Index assignment is now complete.\n");
	printf("\n  |%d| = NumberOfLivingNodes from after the Dangling process.\n", NumberOfLivingNodes);
	printf("  |%d| = IndexCount from the breadth-first assignment function.\n", IndexCount);

	// Allocate the space needed to store the "DawgArray".
	Result->DawgArray = (ArrayDnodePtr)calloc((NumberOfLivingNodes + 1), sizeof(ArrayDnode));
	int IndexFollow = 0;
	int IndexFollower = 0;
	int TransposeCount = 0;
	// Roll through the pointer arrays and use the "ArrayDnodeTnodeTranspose" function to populate it.
	// Set the dummy entry at the beginning of the array.
	ArrayDnodeInit(&(Result->DawgArray[0]), 0, 0, 0, 0, 0, 0);
	Result->First = 1;

	printf("\nStep 13 - Populate the new Working-Array-Dawg structure, used to verify validity and create the final integer-graph-encodings.\n");
	// Scroll through "HolderOfAllTnodePointers" and look for un"Dangling" "Tnodes", if so then transpose them into "Result->DawgArray".
	for ( X = MAX - 1; X >= 0; X-- ) {
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			for (Z = 0; Z < 2; Z++ ) {
				for ( W = 0; W < NodeGroupCounterInit[X][Y][Z]; W++ ) {
					if ( TnodeDangling(HolderOfAllTnodePointers[X][Y][Z][W]) == FALSE ) {
						IndexFollow = TnodeArrayIndex(HolderOfAllTnodePointers[X][Y][Z][W]);
						ArrayDnodeTnodeTranspose(&(Result->DawgArray[IndexFollow]), HolderOfAllTnodePointers[X][Y][Z][W]);
						TransposeCount += 1;
						if ( IndexFollow > IndexFollower ) IndexFollower = IndexFollow;
					}
				}
			}
		}
	}
	printf("\n  |%d| = IndexFollower, which is the largest index assigned in the Working-Array-Dawg.\n", IndexFollower);
	printf("  |%d| = TransposeCount, holds the number of Tnodes transposed into the Working-Array-Dawg.\n", TransposeCount);
	printf("  |%d| = NumberOfLivingNodes.  Make sure that these three values are equal, because they must be.\n", NumberOfLivingNodes);
	if ( (IndexFollower == TransposeCount) && (IndexFollower == NumberOfLivingNodes) ) printf("\n  Equality assertion passed.\n");
	else printf("\n  Equality assertion failed.\n");
	
	// Conduct dynamic-memory-cleanup and free the whole Raw-Trie, which is no longer needed.
	for ( X = MAX - 1; X >= 0; X-- ) {
		for ( Y = 0; Y < NUMBER_OF_ENGLISH_LETTERS; Y++ ) {
			for ( W = 0; W < (NodeGroupCounterInit[X][Y][0] + NodeGroupCounterInit[X][Y][1]); W++ ) {
				free(HolderOfAllTnodePointers[X][Y][2][W]);
			}
			free(HolderOfAllTnodePointers[X][Y][2]);
			free(HolderOfAllTnodePointers[X][Y]);
		}
		free(HolderOfAllTnodePointers[X]);
	}
	free(HolderOfAllTnodePointers);
	free(TemporaryTrie);
	
	printf("\nStep 14 - Creation of the traditional-DAWG is complete, so store it in a binary file for use.\n");
	
	FILE *Data;
	Data = fopen( TRADITIONAL_DAWG_DATA,"wb" );
	// The "NULL" node in position "0" must be counted now.
	int CurrentNodeInteger = NumberOfLivingNodes + 1;
	// It is critical, especially in a binary file, that the first integer written to the file be the number of nodes stored in the file.
	fwrite( &CurrentNodeInteger, sizeof(int), 1, Data );
	// Write the "NULL" node to the file first.
	CurrentNodeInteger = 0;
	fwrite( &CurrentNodeInteger, sizeof(int), 1, Data );
	for ( X = 1; X <= NumberOfLivingNodes ; X++ ){
		CurrentNodeInteger = (Result->DawgArray)[X].Child;
		CurrentNodeInteger <<= CHILD_BIT_SHIFT;
		CurrentNodeInteger += ((Result->DawgArray)[X].Letter) - 'A';
		if ( (Result->DawgArray)[X].EndOfWordFlag == TRUE ) CurrentNodeInteger |= END_OF_WORD_BIT_MASK;
		if ( (Result->DawgArray)[X].Next == 0 ) CurrentNodeInteger |= END_OF_LIST_BIT_MASK;
		fwrite( &CurrentNodeInteger, sizeof(int), 1, Data );
	}
	fclose(Data);
	printf( "\n  The Traditional-DAWG-Encoding data file is now written.\n" );
	
	printf("\nStep 15 - Output a text file with all the node information explicitly layed out.\n");
	
	FILE *Text;
	Text = fopen(TRADITIONAL_DAWG_TEXT_DATA,"w");

	char TheNodeInBinary[32+5+1];
	
	int CompleteThirtyTwoBitNode;
	
	fprintf(Text, "Behold, the |%d| Traditional DAWG nodes are decoded below:\r\n\r\n", NumberOfLivingNodes);
	
	// We are now ready to output to the text file, and the "Main" intermediate binary data file.
	for ( X = 1; X <= NumberOfLivingNodes ; X++ ){
		CompleteThirtyTwoBitNode = (Result->DawgArray)[X].Child;
		CompleteThirtyTwoBitNode <<= CHILD_BIT_SHIFT;
		CompleteThirtyTwoBitNode |= (Result->DawgArray)[X].Letter - 'A';
		if ( (Result->DawgArray)[X].EndOfWordFlag == TRUE ) CompleteThirtyTwoBitNode |= END_OF_WORD_BIT_MASK;
		if ( (Result->DawgArray)[X].Next == 0 ) CompleteThirtyTwoBitNode |= END_OF_LIST_BIT_MASK;
		ConvertIntNodeToBinaryString(CompleteThirtyTwoBitNode, TheNodeInBinary);
		fprintf(Text, "N%6d-%s, DistanceToEndOfList|%2d|", X, TheNodeInBinary, (Result->DawgArray)[X].Position);
		fprintf(Text, ", Lev|%2d|", (Result->DawgArray)[X].Level);
		fprintf(Text, ", {'%c',%d,%6d", (Result->DawgArray)[X].Letter, (Result->DawgArray)[X].EndOfWordFlag, (Result->DawgArray)[X].Next);
		fprintf(Text, ",%6d}", (Result->DawgArray)[X].Child);
		fprintf(Text, ".\r\n");
		if ( CompleteThirtyTwoBitNode == 0 ) printf("\n  Error in node encoding process.\n");
	}
	
	fprintf(Text, "\r\nNumber Of Living Nodes |%d| Plus The NULL Node.\r\n\r\n", NumberOfLivingNodes);

	fclose(Text);
	
	printf("\nStep 16 - Creation of Traditional-DAWG-Encoding file complete.\n");

	return Result;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



int main(int argc, char *argv[]){
	   
    printf("\n  The 17-Step Traditional-DAWG-Creation-Process has commenced: (Hang in there, it will be over soon.)\n");
	int X;
	int Y;
	// All of the words of similar length will be stored sequentially in the same array so that there will be (MAX + 1)  arrays in total.
	// The Smallest length of a string is assumed to be 2.
	char *AllWordsInEnglish[MAX + 1];
	for ( X = 0; X < (MAX + 1); X++ ) AllWordsInEnglish[X] = NULL;
	
	// Read the precompiled lookup-table from "CRC-8.dat" directly into "TheLookupTable".
	FILE *TableFile;
	TableFile = fopen(LOOKUP_TABLE_DATA, "rb");
	fread(TheLookupTable, sizeof(unsigned int), TWO_UP_EIGHT, TableFile);
	fclose(TableFile);
	
	FILE *Input;
	Input = fopen(RAW_LEXICON,"r");
	char ThisLine[100] = "\0";
	int FirstLineIsSize;
	int LineLength;
	
	fgets(ThisLine, 100, Input);
	CutOffExtraChars(ThisLine);
	FirstLineIsSize = StringToPositiveInt(ThisLine);
	
	printf("\n  FirstLineIsSize = Number-Of-Words = |%d|\n", FirstLineIsSize);
	int DictionarySizeIndex[MAX + 1];
	for ( X = 0; X <= MAX; X++ ) DictionarySizeIndex[X] = 0;
	char **LexiconInRam = (char**)malloc(FirstLineIsSize*sizeof(char *)); 
	
	// The first line is the Number-Of-Words, so read them all into RAM, temporarily.
	for ( X = 0; X < FirstLineIsSize; X++ ) {
		fgets(ThisLine, 100, Input);
		CutOffExtraChars(ThisLine);
		LineLength = strlen(ThisLine);
		MakeMeAllCapital(ThisLine);
		if ( LineLength <= MAX ) DictionarySizeIndex[LineLength] += 1;
		LexiconInRam[X] = (char *)malloc((LineLength + 1)*sizeof(char));
		strcpy(LexiconInRam[X], ThisLine);
	}
	printf("\n  Word-List.txt is now in RAM.\n");
	// Allocate enough space to hold all of the words in strings so that we can add them to the trie by length.
	for ( X = 2; X < (MAX + 1); X++ ) AllWordsInEnglish[X] = (char *)malloc((X + 1)*DictionarySizeIndex[X]*sizeof(char));
	
	int CurrentTracker[MAX + 1];
	for ( X = 0; X < (MAX + 1); X++ ) CurrentTracker[X] = 0;
	int CurrentLength;
	// Copy all of the strings into the halfway house 1.
	for ( X = 0; X < FirstLineIsSize; X++ ) {
		CurrentLength = strlen(LexiconInRam[X]);
		// Simply copy a string from its temporary ram location to the array of length equivalent strings for processing in making the DAWG.
		if ( CurrentLength <= MAX ) strcpy( &((AllWordsInEnglish[CurrentLength])[(CurrentTracker[CurrentLength]*(CurrentLength + 1))]),
		LexiconInRam[X] );
		CurrentTracker[CurrentLength] += 1;
	}
	printf("\n  The words are now stored in an array according to length.\n\n");
	// Make sure that the counting has resulted in all of the strings being placed correctly.
	for ( X = 0; X < (MAX + 1); X++ ) {
		if ( DictionarySizeIndex[X] == CurrentTracker[X] ) printf("  |%2d| Letter word count = |%5d| is verified.\n", X, CurrentTracker[X]);
		else printf("  Something went wrong with |%2d| letter words.\n", X);
	}
	
	// Free the the initial dynamically allocated memory.
	for ( X = 0; X < FirstLineIsSize; X++ ) free(LexiconInRam[X]);
	free(LexiconInRam);
	
	printf("\n  Begin Creator init function.\n\n");
	
	ArrayDawgPtr Adoggy = ArrayDawgInit(AllWordsInEnglish, DictionarySizeIndex);
	
	printf("\nStep 17 - Display the Mask-Format for the DAWG int-nodes:\n\n");
	
	char Something[32+5+1];
	ConvertIntNodeToBinaryString(END_OF_WORD_BIT_MASK, Something);
	printf("  %s - END_OF_WORD_BIT_MASK\n", Something);
	
	ConvertIntNodeToBinaryString(END_OF_LIST_BIT_MASK, Something);
	printf("  %s - END_OF_LIST_BIT_MASK\n", Something);
	
	ConvertIntNodeToBinaryString(CHILD_INDEX_BIT_MASK, Something);
	printf("  %s - CHILD_INDEX_BIT_MASK\n", Something);
	
	ConvertIntNodeToBinaryString(LETTER_BIT_MASK, Something);
	printf("  %s - LETTER_BIT_MASK\n", Something);
	
	return 0;
}
