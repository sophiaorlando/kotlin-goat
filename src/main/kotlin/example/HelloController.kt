/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.io.File
import java.util.concurrent.TimeUnit
import java.sql.Statement
import java.sql.DriverManager

/**
 * @author James Kleeh
 * @since 1.0
 */
@Controller("/")
class HelloController(
    private val greetingService: GreetingService
) {

    @Get("/hello/{name}")
    fun hello(name: String): String {
        var contents: String = ""
        File(name).forEachLine { contents += it }

        // Command injection
        runCommand(name)

        // double SQL injection
        validateUser(name, name)

        // TODO - XSS

        // TODO - CSRF
        
        return greetingService.greet(contents)
    }

    private fun runCommand(command: String) {
        val process = Runtime.getRuntime().exec(command)
    }

    val db = DriverManager.getConnection("jdbc:sqlite::memory:").apply {
        createStatement().apply {
            execute("CREATE TABLE USER (ID INTEGER PRIMARY KEY, USERNAME VARCHAR(30) NOT NULL, PASSWORD VARCHAR(30) NOT NULL)")
            execute("INSERT INTO USER (USERNAME,PASSWORD) VALUES ('thomasnield','password123')")
            execute("INSERT INTO USER (USERNAME,PASSWORD) VALUES ('bobmarshal','batman43')")
            close()
        }
    }

    fun validateUser(username: String, password: String): Boolean {
        val statement = db.createStatement()

        val sql = "SELECT COUNT(*) FROM USER WHERE USERNAME = '" + username + "' AND PASSWORD = '" + password  + "'"

        println(sql)

        val rs = statement.executeQuery(sql)
        rs.next()

        val result = rs.getInt(1) > 0
        statement.close()
        return result
    }
}