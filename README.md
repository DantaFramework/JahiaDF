# Danta - JahiaDF Project

Danta is the agnostic multi-platform templating engine. enables developers and IT teams to use technologies they already know, expediting the creation and leveraging of reusable technical assets.

Danta - JahiaDF (Digital Factory) Project is the maven project contained source codes specifically for Jahia.

## Prerequisites

 * [Danta - Parent Project](https://github.com/DantaFramework/Parent)
 * [Danta - API Project](https://github.com/DantaFramework/API)
 * [Danta - Core Project](https://github.com/DantaFramework/Core)
 * Java 8
 * Jahia 7.2 or later

## Documentation

### Installation

  * Clone the following repositories into the same folder (i.e. C:\workspace\danta or /User/{username}/workspace/danta) 
    then run the maven build command (refer to **Compile** section of README.md, for each repository) in the following order:
    * [Parent](https://github.com/DantaFramework/Parent)
    * [API](https://github.com/DantaFramework/API)
    * [Core](https://github.com/DantaFramework/Core)
    * [JahiaDF](https://github.com/DantaFramework/JahiaDF)
    * [JahiaDFContentDefinition](https://github.com/DantaFramework/JahiaDFContentDefinition)
    * [JahiaDF Demo (optional)](https://github.com/DantaFramework/JahiaDFDemo)

### Official documentation

 * Read our [official documentation](http://danta.tikaltechnologies.io/docs) for more information.

## License

Read [License](LICENSE) for more licensing information.

## Contribute

Read [here](CONTRIBUTING.md) for more information.

## Compile

    mvn clean install

## Deploy to Jahia via web console (using Maven Sling Plugin )

    mvn clean install -Pdeploy-jahia

## Deploy to Jahia via file system (using Jahia Maven Plugin)

Edit your Maven settings.xml (usually in ~/.m2/settings.xml) to add the following profile:

        <profile>
            <id>jahia-local</id>
            <properties>
                <jahia.deploy.targetServerType>tomcat</jahia.deploy.targetServerType>
                <jahia.deploy.targetServerVersion>7</jahia.deploy.targetServerVersion>
                <jahia.deploy.targetServerDirectory><path_to_your_digital_factory_folder>/tomcat</jahia.deploy.targetServerDirectory>
            </properties>
        </profile>

Make sure you modify the jahia.deploy.targetServerDirectory to point to the /tomcat directory inside your Jahia installation.

Then you should be able to deploy your module using the following Maven command:

    mvn clean install jahia:deploy -Pjahia-local
    
## Credit

Special thanks to Jose Alvarez, who named Danta for the powerful ancient Mayan pyramid, La Danta. 
La Danta is the largest pyramid in El Mirador—the biggest Mayan city found in Petén, Guatemala.