version=$(xpath -q -e project/version/ pom.xml | sed -e 's/<[^>]*>//g')
artifactId=$(xpath -q -e project/artifactId/ pom.xml | sed -e 's/<[^>]*>//g')

target_file="target/$artifactId-$version.jar"
if [[ ! -f "$target_file" ]]; then
  echo "ERROR | Target file doesn't exist. Use the package script to build it."
  exit 1
fi

scp -p "$target_file" "lightning-remote:/home/lightning/artifacts/$artifactId.jar"
echo "Deploy | $artifactId plugin sent on remote server."