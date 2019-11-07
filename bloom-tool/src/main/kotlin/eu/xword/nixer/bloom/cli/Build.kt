package eu.xword.nixer.bloom.cli

/**
 * Created on 26/09/2019.
 *
 * @author gcwiak
 */
class Build : InputStreamingCommand(name = "build",
        help = """
        Creates a new Bloom filter and inserts values from the given input. 
        Each line is a separate value.
        
        Works as combination of 'create' and 'insert' commands.  
        
        The created filter is represented by two files, the first one contains filter parameters, 
        the second one is data file sized to fit the provided number of expected insertions.
    """) {

    private val basicFilterOptions by BasicFilterOptions().required()
    private val detailedFilterOptions by DetailedFilterOptions().required()

    override fun run() {

        val inputStream = inputStream()

        val bloomFilter = createFilter(
                basicFilterOptions.name,
                detailedFilterOptions.size,
                detailedFilterOptions.fpp
        )

        val entryParser = entryParser()

        val entryHasher: (String) -> String = when {
            hashInput -> ::sha1
            else -> { it -> it }
        }

        val entryTransformer: (String) -> String = { entryHasher(entryParser(it)) }

        tryExecuting {
            insertIntoFilter(bloomFilter, entryTransformer, inputStream)
        }
    }
}