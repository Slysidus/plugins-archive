# Update the Core dependency
(cd ../Core && mvn deploy -DskipTests)

# Generate map templates
cp pom.xml pom.xml.bak
sed -i '/<scope>provided<\/scope>/d' pom.xml
mvn exec:java -Dexec.mainClass=net.lightning.mapmaker.external.TemplatesGenerator
rm pom.xml
mv pom.xml.bak pom.xml

# Package and deploy to local repo if distributionManagement is configured
command=$([[ -n "$(xpath -q -e project/distributionManagement/ pom.xml)" ]] && echo "deploy" || echo "package")
mvn -Duser.name="Lightning" "$command"
