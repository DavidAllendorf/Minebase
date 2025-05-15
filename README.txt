DEV:
In plugin.yml add:
    depend: [Minebase]

GRADLE:
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
    	        implementation 'com.github.DavidAllendorf:Minebase:alpha'
    }

MAVEN:
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependency>
        <groupId>com.github.DavidAllendorf</groupId>
        <artifactId>Minebase</artifactId>
        <version>alpha</version>
    </dependency>

SCHEMA_EXAMPLE:
{
    "Player": {
        "table": "player",
        "columns":
            {
                name: uuid,
                type: INTEGER
            },
            {
                name: name,
                type: TEXT
            },
            {
                name: progress,
                type: REAL
            }
    }
}

EXAMPLE_CODE:
    Minebase api = getServer().getPluginManager().getPlugin("Minebase").getApi();
    api.loadSchema(); //Call on onCreate!

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
        while (rs.next()) { //Zeile
            getLogger().info("Row: " + rs.getString("name")); //Spalte
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
