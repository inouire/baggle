#!/bin/bash
# Build server and release it on baggle-server-deploy repository

server_deploy_path="../../baggle-server-deploy"
version=$1

# build baggle-server and move generated files
ant
cp dist/baggle-server.jar $server_deploy_path/bin
cp dist/lib/*.jar $server_deploy_path/bin/lib
echo $version > $server_deploy_path/SERVER_VERSION

# perform git operations
cd $server_deploy_path
git status
echo "Do you wish to finalise release process with git comamnds?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) break;;
        No ) exit;;
    esac
done
git checkout v3
git stage bin
git stage SERVER_VERSION
git commit -m "Upgrading to baggle-server-$version"
git tag $version
git push origin v3 --tags

