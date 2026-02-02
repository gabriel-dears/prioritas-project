// prioritas-project/build.gradle.kts

plugins {
    // Aplica o plugin base para habilitar tarefas padrão de ciclo de vida
    base
}

// Configuração para limpar as pastas 'build' de todos os subprojetos quando rodar 'clean' na raiz
tasks.register("cleanAll") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}

// Opcional: Se quiser garantir que todos usem os mesmos repositórios
allprojects {
    repositories {
        mavenCentral()
    }
}