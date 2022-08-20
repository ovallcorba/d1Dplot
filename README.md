# d1Dplot

d1Dplot is a plotting program for 1D X-ray diffraction (XRD) data (i.e. powder diffraction patterns). Its main purposes are to be user friendly, to provide specific tools for XRD data processing/analysis and the possibility to generate nice plots and figures for publications or reports.

Detailed information can be found in the [users guide](d1Dplot_userguide.pdf) or in the publication ["XRD data visualization, processing and analysis with d1Dplot and d2Dplot software packages" *Multidisciplinary Digital Publishing Institute Proceedings* 62 (1), 9](https://www.mdpi.com/2504-3900/62/1/9)

d1Dplot was a personal closed source project started on 2015 and actively maintained until 2020. From August 2022 the program is open source and licensed under the GPL-3.0 license. Sorry if the code is not compliant with standards, is a little bit messy and contains comments in catalan.

### Dependencies

d1Dplot is completely programmed with JavaTM (www.java.com) using OpenJDK version 11.0.9.1 (GNU General Public License, version 2, with the Classpath Exception: https://openjdk.java.net/legal/gplv2+ce.html). You may find Oracle's free, GPL-licensed, production-ready OpenJDK binaries necessary to run d1Dplot at https://openjdk.java.net/.

The following 3rd party libraries have been used:
- MigLayout. http://www.miglayout.com
    BSD license: http://directory.fsf.org/wiki/License:BSD_4Clause
- Commons Math. https://commons.apache.org/proper/commons-math/
    Apache License: http://www.apache.org/licenses/LICENSE-2.0
- Apache Batik. https://xmlgraphics.apache.org/batik/
    Apache License: http://www.apache.org/licenses/LICENSE-2.0

(No changes on the source codes of these libraries have been made, you can download the source codes for these libraries at their respective websites).

The program also uses the following libraries from the same author (packages com.vava33.*)

- BasicPlotPanel. https://github.com/ovallcorba/BasicPlotPanel
- vavaUtils (jutils and cellsymm). https://github.com/ovallcorba/vavaUtils

### Installation and use

Binaries for windows and linux can be downloaded in the releases section (https://github.com/ovallcorba/D1Dplot/releases). Otherwise you need to clone the project, gather the dependencies and generate the jar files.

## Authors

  - **Oriol Vallcorba**

## Disclaimer

This software is distributed WITHOUT ANY WARRANTY. The authors (or their institutions) have no liabilities in respect of errors in the software, in the documentation and in any consequence of erroneous results or damages arising out of the use or inability to use this software. Use it at your own risk.

## Acknowledgments 

Thanks are due the Spanish "Ministerio de Ciencia e Innovación", to the "Generalitat the Catalunya" and to the ALBA Synchrotron for continued financial support.

## License

This project is licensed under the [GPL-3.0 license](LICENSE.txt)

Citation of the author/program/affiliation, e.g. O. Vallcorba & J. Rius. XRD data visualization, processing and analysis with d1Dplot and d2Dplot software packages. (doi:10.3390/IOCC_2020-07311), would be greatly appreciated when this program helped to your work.