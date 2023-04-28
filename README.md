## Boswell
Boswell is a Maplestory emulator project that slowly is rewriting OdinMS 
into something more modern using Kotlin! Luckily, this process is made 
easy with Kotlin's 100% interoperability with Java! 

Boswell is based on a project by the same name. That project 
can be found [here](https://github.com/boswell83/boswell).

-------------------------------------------------------------------

## Scripting

Boswell now uses stateless scripting written in Groovy. This means that 
all pre-existing javascript scripts are no longer functional and need to be rewritten. 
If you would like to use the old odinms scripting style you can find that 
in the `odinms-scripting` branch or in the project Releases.

Documentation on how to write these new scripts can be found [here](https://github.com/conchlin/boswell/tree/main/scripts).

-------------------------------------------------------------------

## Requirements/Dependencies
- [x] Java (17+)
- [x] Kotlin (1.7.10)
- [x] Postgresql (42.2.9+)
- [x] groovy-jsr223 (3.0.16)
- [x] gson (2.8.6+)
- [x] HikariCP (5.0.1+)
- [x] Apache.mina (2.0.7+)
- [x] slf4j (1.6.6+)
- [x] jUnit (5.9.2+)

-------------------------------------------------------------------

Development is slow since it's just me working on it. However, if you're at 
all interested in the project or have questions Boswell can be found on 
[discord](https://discord.gg/dFuG462yHX)! 