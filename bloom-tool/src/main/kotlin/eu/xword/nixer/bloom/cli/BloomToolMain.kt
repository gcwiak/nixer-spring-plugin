package eu.xword.nixer.bloom.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.long
import com.google.common.base.Charsets.UTF_8
import com.google.common.hash.Funnel
import com.google.common.hash.Funnels
import eu.xword.nixer.bloom.Benchmark
import eu.xword.nixer.bloom.BloomFilter
import eu.xword.nixer.bloom.FileBasedBloomFilter
import eu.xword.nixer.bloom.HexFunnel
import java.io.InputStreamReader
import java.nio.file.Paths

/**
 * The entry class for a command utility to manipulate file-based bloom filters.
 * <br></br>
 * Created on 23/08/2018.
 *
 * @author cezary.biernacki@crosswordcybersecurity.com
 */
class BloomToolMain : CliktCommand(name = "bloom-tool") {
    override fun run() = Unit // Nothing here, the actual work is done by subcommands.
}

fun main(args: Array<String>) = BloomToolMain()
        .subcommands(
                Create(),
                Insert(),
                Check(),
                BenchmarkCmd()
        )
        .main(args)

class Create : BloomFilterAwareCommand(name = "create", help = "Creates a new bloom filter.") {

    private val size: Long by option(help = "Expected number of elements to be inserted").long().required()

    private val fpp: Double by option(help = "Target maximum probability of false positives").double().default(1e-6)

    override fun run() {
        FileBasedBloomFilter.create(
                Paths.get(name),
                getFunnel(),
                size,
                fpp
        )
    }
}

class Insert : BloomFilterAwareCommand(name = "insert",
        help = """
        Inserts values to the filter from standard input.
        Each line is a separate value.
    """) {

    override fun run() {
        val bloomFilter = openFilter()

        InputStreamReader(System.`in`, UTF_8.newDecoder()).buffered().use { reader ->
            reader.lines().forEach { bloomFilter.put(it) }
        }
    }
}

class Check : BloomFilterAwareCommand(name = "check",
        help = """
        Checks if values provided in the standard input appear in the filter,
        printing matches to the standard output, and skipping not matched values.
        Each line is a separate value.
    """) {

    override fun run() {
        val bloomFilter = openFilter()

        InputStreamReader(System.`in`, UTF_8.newDecoder()).buffered().use { reader ->
            reader.lines()
                    .filter { bloomFilter.mightContain(it) }
                    .forEach { println(it) }
        }
    }
}

abstract class BloomFilterAwareCommand(name: String, help: String) : CliktCommand(name = name, help = help) {

    protected val name: String by argument(help = """
            Name of the bloom filter. Corresponds to name of the file with filter parameters and prefix of the data file.
            """)

    private val hex: Boolean by option(help = """
            Interprets input values as hexadecimal string when inserting or checking.
            Values are converted to bytes before inserting, if this conversion fail,
            the string is inserted a normal way.
            """)
            .flag()

    protected fun openFilter(): BloomFilter<CharSequence> = FileBasedBloomFilter.open(
            Paths.get(name),
            getFunnel()
    )

    protected fun getFunnel(): Funnel<CharSequence> = when {
        hex -> HexFunnel(Funnels.unencodedCharsFunnel())
        else -> Funnels.unencodedCharsFunnel()
    }
}

class BenchmarkCmd : CliktCommand(name = "benchmark",
        help = """
        Runs performance benchmark and correctness verification
        by creating a filter in a temporary directory,
        populating it with random data and checking what can be found.
    """) {

    private val size: Long by option(help = "Expected number of elements to be inserted").long().required()

    private val fpp: Double by option(help = "Target maximum probability of false positives").double().default(1e-6)

    override fun run() {
        Benchmark(size, fpp).run()
    }
}
