package dev.takeda.androidguidelines

import dev.takeda.androidguidelines.server.AndroidGuidelinesServer
import dev.takeda.androidguidelines.source.SourceLoader
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

fun main(): Unit =
    runBlocking {
        // All logs MUST go to stderr — stdout is reserved for the JSON-RPC stream.
        val log = { msg: String -> System.err.println("[android-guidelines-mcp] $msg") }

        val dataRoot = SourceLoader.defaultDataRoot()
        log("Loading guidelines from $dataRoot")

        val sections = SourceLoader(dataRoot).loadAll()
        if (sections.isEmpty()) {
            log("WARNING: no sections loaded.")
            log("Run scripts/fetch-guidelines.sh and ensure ANDROID_GUIDELINES_DATA points to .guidelines-data.")
        } else {
            log("Loaded ${sections.size} sections from ${sections.map { it.sourceId }.distinct().size} sources")
        }

        val server = AndroidGuidelinesServer.build(sections)

        val transport =
            StdioServerTransport(
                inputStream = System.`in`.asSource().buffered(),
                outputStream = System.out.asSink().buffered(),
            )

        val done = Job()
        server.onClose { done.complete() }
        server.connect(transport)
        log("Connected over stdio. Waiting for requests…")
        done.join()
    }
