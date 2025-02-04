# pdftohtml5 

A PDF document translator that produces an equivalent HTML5 document with near pixel-perfect accuracy.

- Character positions are grouped into logical block elements. 
- Embedded PDF fonts are handled by FontForge, and re-encoded as base64 OpenType fonts in the header.

Examples

- [p01apr97.pdf](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01apr97.pdf) ‣ [p01apr97.html](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01apr97.html)
- [p01oct97.pdf](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01oct97.pdf) ‣ [p01oct97.html](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01oct97.html)
- [p01sep02.pdf](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01sep02.pdf) ‣ [p01sep02.html](https://natlaughlin.github.io/pdftohtml5/src/test/resources/p01sep02.html)
- [fw4.pdf](https://natlaughlin.github.io/pdftohtml5/src/test/resources/fw4.pdf) ‣ [fw4.html](https://natlaughlin.github.io/pdftohtml5/src/test/resources/fw4.html)

Requirements

- Java 1.8
- Maven 3.6
- FontForge

Instructions

- `mvn clean package`
- `java -cp target/pdftohtml5-0.0.1-SNAPSHOT.jar com.natlaughlin.pdftohtml5.PdfToHtml5 <OPTIONS> <PDF file>`

TODO

- Some special font types are not currently supported.
- Add more PDF test cases.
  


