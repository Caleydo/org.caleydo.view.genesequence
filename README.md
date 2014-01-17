Gene Sequence View
==============

this is a very basic gene sequence viewer for a specific chromosome.

It automatically registers additional mappings containing:
 gene_symbol 1..n chromosome_location 1..n chromosome
 
in addition, two metadata data domains are created:

chromosome metadata:
name, total length

chromosome_location metadata
start location, end location

all the information are used within the ChromosomeLocationElement, which requires:
 * a list of ids 
 * an idtype, which is a gene one
 
the result is a simple visualization of the most frequent chromosome.