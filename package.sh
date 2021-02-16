# Update the Core dependency
(cd ../Core && mvn deploy -DskipTests)

# Package and deploy to local repo if distributionManagement is configured
command=$([[ -n "$(xpath -q -e project/distributionManagement/ pom.xml)" ]] && echo "deploy" || echo "package")
mvn -Duser.name="Lightning" "$command"
