# nlp-eval-service
A RESTful API for evaluating output of NLP systems.

## Docker Installation (Recommended)
#### Requirements
* Java v8 (or better)
* [Apache Maven](https://maven.apache.org/)
* [Docker](https://www.docker.com/)

#### Installation steps
1. Build the project using Apache Maven by running the following command from inside this repository:

    `mvn clean package`

2. then, build the Docker container:

    `docker build -t nlp-eval-service:0.1.0 .`

3. Once the container has been built, it can be started by the following command:

    `docker run -d --name nlp-eval-service -p 8080:8080 nlp-eval-service:0.1.0`

## Local Installation
#### Requirements
* Java v8 (or better)
* [Apache Maven](https://maven.apache.org/)
* [Clojure Boot](https://github.com/boot-clj/boot)

#### Installation steps
1. Download a [distribution of the CRAFT corpus](https://github.com/UCDenver-ccp/CRAFT/releases) (version >= 3.1).

1. Unpack the CRAFT distribution into a directory of your choosing, and then create the BioNLP formatted files for all concept annotation types. In the base directory of the CRAFT distribution, run the following command:

    `boot all-concepts convert -b`

1. Edit the `src/main/resources/application.properties` file and update the `file.craft-dir` parameter to point to the base directory of your local CRAFT distribution.

1. Build the project using Apache Maven:

    `mvn clean package`

1. Finally, start the service by running:

    `java -jar target/nlp-eval-service-0.1.0.jar`


## Using the service via the Web UI
In a web browser, visit `http://<host-name>:8080` where *<host-name>* is the IP address or name of the host where you have installed the NLP Evaluation Service. If the service is installed on your local machine, then *<host-name>* will be *localhost*, i.e. [http://localhost:8080](http://localhost:8080).

1. Upload the test annotation files in the BioNLP format that are to be evaluated by selecting them using the *Choose Files* button. 

2. Select the ontology relevant to the files you have uploaded.

3. Select the concept mention boundary match strategy to use. *EXACT* is a binary strategy; the entire boundary is either correct or it is incorrect. The *JACCARD* option allows for partial credit if a mention boundary overlaps with the gold standard, but is not exactly the same as the gold standard.

4. Click on the *Evaluate* button. Results will be displayed when the evaluation is complete.

## Using the service via the command line

From the command line, the `curl` command can be used to submit evaluation requests. The URL to target is `http://<host-name>:8080` where *<host-name>* is the IP address or name of the host where you have installed the NLP Evaluation Service. If the service is installed on your local machine, then *<host-name>* will be *localhost*, i.e. [http://localhost:8080](http://localhost:8080). 

Each file is specified using `-F files=@"<file-name>"`, where *<file-name>* is the name of a file to be evaluated, e.g. 11319941.bionlp. Multiple `-F` blocks can be added to evaluate multiple files.

The ontology is specified by the `ont=<ontology-key>` parameter and the boundary matching strategy is specified by the `bms=<matching-strategy>` parameter.

Valid ontology keys include: 
`CHEBI, CL, GO_BP, GO_CC, GO_MF, MOP, NCBITaxon, PR, SO, UBERON`
`CHEBI_EXT, CL_EXT, GO_BP_EXT, GO_CC_EXT, GO_MF_EXT, MOP_EXT, NCBITaxon_EXT, PR_EXT, SO_EXT, UBERON_EXT`

Valid boundary matching strategies include `EXACT` to require exact concept mention boundary matches and `JACCARD` to allow for non-exact, i.e. boundaries that overlap, boundary matches.

Compose the `curl` command like the following:

   `curl -F files=@"<file-name1>" -F files=@"<file-name2>" 'http://<host-name>:8080/eval?ont=<ontology-key>&bms=<matching-strategy>'`

For example: 

   `curl -F files=@"11319941.bionlp" -F files=@"15018652.bionlp" -F files=@"17677002.bionlp" -F files=@"16026622.bionlp" 'http://localhost:8080/eval?ont=CL&bms=JACCARD'`


## File format
Annotation files submitted for evaluation must use the pre-defined, stand-off [BioNLP format](http://2013.bionlp-st.org/file-formats). There should be a single annotation file for each document, named with the document ID appended with `.bionlp`, e.g. `11319941.bionlp`. Each line of the file represents a single annotation and takes the form: 
`T<num> [TAB] <ontology-id> [SPACE] <span> [TAB] <covered-text>` where,
* `<num>` is an integer that is unique for each annotation
* `<ontology-id>` is the ontology identifier asserted by the annotation, e.g. `CL:0000604`
* `<span>` is the character offsets using UTF-8 encoding for the annotation. Spans are represented by two integers separated by a space, e.g. `<span-start> [SPACE] <span-end>`. It is possible for an annotation to have a discontinuous span. In such cases, multiple spans are delimited by a semi-colon, e.g. `<span1-start> [SPACE] <span1-end>;<span2-start> [SPACE] <span2-end>`.
* `<covered-text>` is the text for the annotation as seen in the original document. If the annotation is comprised of discontinuous spans, then an ellipsis will be shown in the text.

Example annotation in the expected BioNLP format:
`T3      CL:0000604 30862 30865;30875 30889      rod ... photoreceptors`


## Evaluation Metrics
Annotations are evaluated on a per ontology basis, i.e. separately for each ontology. This service computes the Slot Error Rate and F-score as described in [Bossy et al. (2013) BioNLP shared Task 2013 -- An Overview of the Bacteria Biotope Task. Proc BioNLP Shared Task 2013 Workshop. 161-169.](https://www.aclweb.org/anthology/W13-2024/).

## Ontologies
There are ten ontologies with accompanying manually curated annotations that can be used as a gold standard for concept recognition evaluation. For each of these ten ontologies, two annotation sets have been created. The first, called the *core* set, consists solely of annotations made with proper classes of the original given ontology. The second, called the *core+extensions* set, consists of annotations made with these proper ontology classes as well as annotations made with what we refer to as extension classes, which are classes that were created as extensions of the ontologies, but defined in terms of proper ontology classes. These extension classes have been created for various reasons, including semantic unification of classes from different ontologies, unification of multiple classes that were difficult to reliably use for annotation, creation of semantic abstractions to annotate correspondingly abstract textual mentions, and representation of corresponding types of concepts that were found easier to use for annotation compared to their original forms. An extension class is identifiable by its namespace prefix, which always ends in "_EXT". For further details, please see this [description of the CRAFT concept annotations](https://github.com/UCDenver-ccp/CRAFT/blob/master/concept-annotation/README.md).

* Chemical Entities of Biological Interest (CHEBI)
* Cell Ontology (CL)
* Gene Ontology Biological Process (GO_BP)
* Gene Ontology Cellular Component (GO_CC)
* Gene Ontology Molecular Function (GO_MF)
* Molecular Process Ontology (MOP)
* NCBI Taxonomy (NCBITaxon)
* Protein Ontology (PR)
* Sequence Ontology (SO)
* Uberon (UBERON)


## Feedback
If you experience issues with the service, notice an error, or have a suggestion, please submit an issue to the [Issue Tracker](https://github.com/UCDenver-ccp/nlp-eval-service/issues) of this project.

## Acknowledgements
The framework underlying this service borrows heavily from callicoder's [Spring Boot File Upload / Download Rest API Example](https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example).
