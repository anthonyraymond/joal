#!/usr/bin/env bash

is_snapshot() {
    [[ "$1" == *"-SNAPSHOT" ]]
}
remove_snapshot_safe() {
    local version="$1"
    echo "$version" | sed s/-SNAPSHOT//
}
increment_version() {
    local version=$1
    local rgx='^((?:[0-9]+\.)*)([0-9]+)($)'

    val=`echo -e "$version" | perl -pe 's/^.*'$rgx'.*$/$2/'`
    echo "$version" | perl -pe s/$rgx.*$'/${1}'`printf %0${#val}s $(($val+1))`/
}


current_version=$(mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Current Version is $current_version"

release_version="$current_version"
if is_snapshot ${current_version}; then
    release_version=$(remove_snapshot_safe "$release_version")
else
    release_version=$(increment_version "$release_version")
fi

read -e -p "Enter release version [$release_version]:" user_input_release_version
release_version="${user_input_release_version:-$release_version}"

# Prevent fail when wildcard matches nothing
shopt -s nullglob

# Update version in all pom.xml
mvn versions:set -DnewVersion="$release_version" -DprocessAllModules=true -DgenerateBackupPoms=false
# create a commit that will hold the tag
git add pom.xml ./*/pom.xml && git commit -m "Prepare release $release_version" && git push origin master

# Tag and push the tag
git tag "$release_version"
git push --tags origin

# prepare next iteration and push
mvn versions:set -DnextSnapshot=true -DprocessAllModules=true -DgenerateBackupPoms=false
git add pom.xml ./*/pom.xml && git commit -m "Prepare next iteration" && git push origin master

shopt -u nullglob















#!/bin/bash

versionPattern="[0-9]+\.[0-9]+\.[0-9]+(\-beta)*"

tagName=""
while [[ ! $tagName =~ $versionPattern ]]; do
  read -p "Tag name (version): " tagName
done


if ! grep -Fq "<version>$tagName</version>" ./pom.xml; then
  echo -e "$(tput setaf 1)WOW, wait! The provided tag does not match the pom.xml version$(tput sgr0)"
  exit 1
fi

if GIT_DIR=./.git git rev-parse $tagName >/dev/null 2>&1
then
    echo -e "$(tput setaf 1)ERROR: Tag $tagName already exists$(tput sgr0)"
fi

if git tag $tagName; then
  echo -e "$(tput setaf 2)Tag created$(tput sgr0)"
else
  echo -e "$(tput setaf 1)ERROR: Failed to create tag locally$(tput sgr0)"
  exit 1
fi

if git push origin $tagName; then
  echo -e "$(tput setaf 2)Tag pushed$(tput sgr0)"
else
  echo -e "$(tput setaf 1)ERROR: Failed to push tag$(tput sgr0)"
  exit 1
fi

echo -e "$(tput setaf 2)Everything went fine :)$(tput sgr0)"
