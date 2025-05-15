Plugin that utilizes a SQLite Database.
This README is for Developers!

DEPENDENCIES:
    In plugin.yml add:
        depend: [Minebase]

    For Development, you need this external Dependencies:
    "com.fasterxml.jackson.core:jackson-databind:2.17.0"
    "org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT"

    GRADLE(xxxxx = Release Version):
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                mavenCentral()
                maven { url 'https://jitpack.io' }
            }
        }

        dependencies {
                    implementation 'com.github.DavidAllendorf:Minebase:xxxxx'
        }

    MAVEN(xxxxx = Release Version):
        <repositories>
            <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
            </repository>
        </repositories>
        <dependency>
            <groupId>com.github.DavidAllendorf</groupId>
            <artifactId>Minebase</artifactId>
            <version>xxxxx</version>
        </dependency>

SCHEMA_EXAMPLE:
    {
      "abc": {
        "table": "abc",
        "columns": [
          { "name": "uuid", "type": "INTEGER" },
          { "name": "name", "type": "TEXT" },
          { "name": "progress", "type": "REAL" }
        ]
      }
    }

EXAMPLE_CODE:
    //Loading the Plugin
    Plugin minebase = getServer().getPluginManager().getPlugin("Minebase");
    MineDb api = ((Minebase) minebase).getApi();
    api.loadSchema(); // The file is created when the plugin is started for the first time.
                         Then stop the server and edit the JSON under Plugins.
                         After editing, the server can be started again.

    //Insert Example
    InsertList insertData = new InsertList();
    ChangeData row = new ChangeData();
    row.put("uuid", "1");
    row.put("name", "Test1");
    row.put("progress", "100");
    insertData.add(row);
    api.create("player",insertData);

    //Update Example
    ChangeData updateData = new ChangeData();
    updateData.put("name", "TestUpdate2");
    api.update("player", updateData, cond("uuid = 1234123"));

    //Delete Example
    api.delete("player", cond("uuid = 1"));

    //Select Example
    ResultSet rs =  api.select("player", col("uuid","name"), cond("uuid = 1234123"), sort("name asc"));
    try {
        while (rs.next()) { //Row
            getLogger().info("Row: " + rs.getString("name")); //Column
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
