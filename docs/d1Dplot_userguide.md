# d1Dplot User's Guide
<div style="text-align: center;">

![](.//Pictures/100000010000008000000080061F9C89CB62047A.png)   
(for version 2101)

Author: **Oriol Vallcorba**   
ALBA Synchrotron Light Source - CELLS (www.cells.es)

For comments/complaints/errors/suggestions, please contact to: **ovallcorba@cells.es**

</div>

***
- [d1Dplot User's Guide](#d1dplot-users-guide)
  - [1. Installation and use of *d1Dplot*](#1-installation-and-use-of-d1dplot)
    - [Configuration file](#configuration-file)
  - [2. Main Window](#2-main-window)
  - [3. Plot Options](#3-plot-options)
    - [2D plot](#2d-plot)
    - [Sequential Y offset](#sequential-y-offset)
  - [4. Processing](#4-processing)
    - [Find Peaks](#find-peaks)
    - [Calc Background](#calc-background)
    - [Subtract Patterns](#subtract-patterns)
    - [Rebinning](#rebinning)
    - [Convert to wavelength](#convert-to-wavelength)
    - [Change X units](#change-x-units)
    - [Sum selected patterns](#sum-selected-patterns)
    - [Fit peaks](#fit-peaks)
  - [5. Tools](#5-tools)
    - [Compound database](#compound-database)
  - [5. Command line mode](#5-command-line-mode)
  - [6. Supported formats](#6-supported-formats)
    - [DAT (ALBA), XYE, XY, ASC](#dat-alba-xye-xy-asc)
    - [GSA](#gsa)
    - [XRDML](#xrdml)
    - [D1P](#d1p)
    - [PRF](#prf)
    - [REF](#ref)
    - [GR](#gr)
  - [7. References](#7-references)
  - [8. Miscellaneous](#8-miscellaneous)
    - [Release notes](#release-notes)
    - [Contact information](#contact-information)
    - [Conditions of use](#conditions-of-use)
    - [Disclaimer](#disclaimer)
    - [Acknowledgments](#acknowledgments)
***

## 1. Installation and use of *d1Dplot* 

No installation of the program is required. Extract the files and folders of the zip file into the desired folder in your hard drive and run the executable file (`d1Dplot.exe` in Windows and `d1Dplot` in Linux). In most of the recent Linux distributions, the executable files can be executed by double click from the file manager but alternatively you can run it from the command line with `./d1Dplot`. You may need to set the execute flag of the file with: `chmod +x d1Dplot`

**Tip**: Running it from the command line has the advantage that you can give a pattern file as the argument and it will be automatically opened.

*Note:* A Java Runtime Environment is required (Java Platform, SE version 6 or higher).

### Configuration file

The first run, the program generates a plain text configuration file `d1dconfig.cfg` at the same folder where the program is installed. However, in some systems it can be created inside the user folder or somewhere else (the program will display the location of the file on the output panel located at the bottom part of the main window). Usually there is no need to change anything of this file but, if desired, the parameters are self-explanatory and their value can be modified.

## 2. Main Window

This is the aspect of the main window after loading a pattern (via menu *File-Open*, or by clicking the button *+*, or from command line).

![](.//Pictures/10000000000004C5000002E447CEE4FBB270C5F8.png)

The main parts are the menu bar (top), the plotting area (center-left), the plot control panel (center-right) and the bottom tab panel. Next sections of the guide will explain each part in detail.

- **Menu bar**. To access all the program modules and options. It contains:
  - **File**
      - *Open Data file*. Opens a pattern file.
      - *Save Data as*. Save the (selected) pattern file to any of the
        supported formats.
      - *Save obs/cal/hkl matching*. Save a pattern matching file.
      - *Export as PNG.* Export plot area as a PNG image. It asks for a
        (optional) factor to create a bigger image.
      - *Export as SVG.* Export plot area as Scalable Vector Graphics
        file.
      - *Save Project*. Save a *d1Dplot* project file.
      - *Open Project*. Open a *d1Dplot* project file.
      - *Close all*. Close all opened patterns.
      - *Quit*. Exit the program
  
  - **Plot Options**
    - *2D plot.* Bidimensional plot of the selected patterns.
    - *Sequential Y offset*. Apply a Y offset (vertical) to the selected patterns sequentially so that they end in a “stacked” position.
    - *Invert pattern order.* Reverse the order of patterns in the table.

  - **Processing**
    - *Find peaks.* Search for peaks (list of peaks can be saved later).
    - *Calc Background.* Estimation of the background.
    - *Subtract patterns*. Subtract patterns (with an optional factor).
    - *Rebinning*. Change 2*ϴ*<sub>ini </sub>step and 2*ϴ*<sub>end</sub> of selected pattern(s)-
    - *Convert to wavelength.* “Convert” the selected pattern to a new wavelength, for comparison purposes in 2theta units.
    - *Change X units.* Change the X units of the selected pattern (to/from 2theta, d-spacing, Q, ...)
    - *Sum selected patterns.* To sum the selected patterns.
    - *Fit Peak(s).* Fit selected peak(s) with a pseudoVoigt profile.

  - **Tools**
    - *Compound database.* Opens the compound database window. To plot expected reflection positions from a user created database.

  - **Help**
    - *About.* Some information about the program.
    - *Manual.* Link to this user's guide.**
    - *Check for updates.* To see if a new version of *d1Dplot* is available.

- **Plot Area**. Where the patterns are shown. The general
interaction is:
  - Left mouse button: Zone selection for zoom in X. Add peaks (peak search module)
  - Middle mouse button: Press and drag to navigate the pattern in X and Y. Click to reset zoom and fit view.
  - Mouse wheel: Zoom on Y.
  - Right mouse button: Pres and drag (UP and DOWN) for zoom on X. Press and drag (LEFT and RIGHT) to navigate the pattern along X. Remove peaks (peak search module)

  These default mouse button assignments can be changed in the options file.

  The plot area has the following status bar at the bottom:

  ![](.//Pictures/10000000000003BE0000001DA8E41D02336A6D1D.png)
  it displays on the left information about the point currently pointed by the cursor and in the right there are 3 buttons: the first to fit window (reset zoom of the plot area), the second to repaint patterns (reassign colors to all shown patterns) and the third to **show/hide the plot options panel** (explained below).

- **Patterns table and log**. Here, there are two tabs:

  - **Data**: It contain a table with all opened patterns. The columns are:
      - *nP*: Pattern number.
      - *nS*: series number.
      - *Name*: Name of the series (by default the filename).
      - *Color*: Color of the series.
      - *Scale*: Y scale of the series.
      - *ZerOff*: Offset of the series in X.
      - *Wavel*: Wavelength of the pattern. Put the correct value to perform operations that need it.
      - *Xunits*: Units of X. By default 2theta. Put the correct value to perform operations that need it.
      - *Y offset*: Offset of the series in Y. 
      - *Marker*: Marker size.
      - *Line*: Line width.
      - *ErrBars*: To show the error bars on Y.
      - *Show*: To show or hide the current series on the plot area.
      - *Type*: Type of data series.
   
    Most of the values can be edited and assigned by clicking directly on the table.
    Buttons to move the patterns up/down, to remove/add, reload or duplicate are available.
    Right click button on the table opens a contextual menu with several options, such as changing values of all selected cells (column-wise) or data series:
    ![](.//Pictures/100000010000041E000000E121281C289BD1F801.png)
    ![](.//Pictures/10000000000001080000008BDC4373A4FB841331.png)
    ![](.//Pictures/100000000000041800000096BE629BB8B93E6679.png)

  - **Log**. Messages of the program are shown here.

- **Plot control panel**. This panel is by default hidden but it can be displayed by clicking on the rightmost button of the status bar () below the plot area. It has the following options:

  ![](.//Pictures/100000000000013C0000033AA106C4E451CBAEF2.png)

  From here we can assign the plot title and axes labels, the color theme, the legend position, the axes divisions, the region currently shown in the plot window (defined by the values *Xmin, Xmax, Ymin, Ymax)*, a scale of a specific zone, grids, show pattern names in the plot, and other display options.

  The divisions of the axes are set with the following values:

  - *iniX, incX, divX*: Initial X value, major tick increments (for labels) and number of subdivisions (without labels). 

  - *iniY, incY, divY*: Same as X but for Y axis.

  Changing a value and pressing enter applies the value. Reset the view with the corresponding reset button or middle mouse button (usually the wheel) click. **Fix X values** is the most important option regarding the behavior of the axes: when activated the values of X incr, Y incr, X sub and Y sub will remain fixed (as if the X axis was stuck with the pattern). By default it is disabled so the ticks and separations are kept and the values of the labels are changing according to how the pattern is moved or zoomed.

  Finally, by clicking the *more…* button, few additional “lower-level” display options and navigation behavior can be set, that will be stored in the configuration file as default for next sessions. The option plot area size allows to create a plot “canvas” bigger or smaller than the plotting area.

  ![](.//Pictures/100000000000028C000001F4578C846B5F55F777.png)

## 3. Plot Options

### 2D plot

To plot several patterns in 2D, select them on the table and click on *Plot Options -\> 2D plot*. 

![](.//Pictures/1000000100000552000003525953CD465AB2E696.png)

A new window will open with the 2D plot:

![](.//Pictures/10000000000003EF0000024B49C5D94B7C1E6A17.png)

On the 2D plot window you can control the contrast with the slider bar on the top of the plot area. The limits of the contrast bar can be changed on the surrounding textboxes (and pressing enter to confirm). On the plot area, zones can be zoomed in by using the mouse (left click + drag). By default the zoom is only on the X dimension but there is the option of *square selection* that allows to select square-shaped areas. In that case, it is useful to enable *auto fit Y* to fit always the zoomed area to the height of the plot area (the button *Fit Y* allows to do it once only). Use fit to window to return to the initial (full) view.

On the right there are several visualization options including the X axis divisions and the option to show the pattern names on the image (e.g. for showing temperatures, pressures,...), and choosing the text color and position. Also the image can be saved as PNG.

### Sequential Y offset

This option can be used to “pile up” diffraction patterns to compare them or check parameter evolutions, phase transitions, etc... Select the patterns you want to stack and a Y offset value between them.

![](.//Pictures/10000000000004C5000002E447CEE4FBB270C5F8.png)

## 4. Processing

### Find Peaks

To find diffraction peaks and create a peak(s) series of the current pattern.

![](.//Pictures/100000000000045E000002CD365C5B379AF83224.png)

The options are:

To perform an Auto Peak Search by using or not a background estimation and a slider to set the threshold. If the slider range is not enough it can be changed by clicking the button *set factMinMax*.

Single peaks can be added by clicking to *Add Peaks*, then selecting the peaks by left-button click and click again to the same button (which will show *Finish*). To remove peaks it is the same procedure but with the *Remove Peaks* button. Clear all the peaks with the *Remove All* button.

Click on save peaks to save the list of peaks as a text file.

### Calc Background

To estimate the background.

![](.//Pictures/100000000000045E000002CD72FACC6C25925C76.png)

Here an estimation of the background can be calculated. It is created as a new series (can be subtracted later if desired or exported as a list of points with *save as...*). There are three methods:

  - Bruchner smoothing procedure (Brüchner, 2000) allowing to use different conditions in function of 2-theta.
  - N-Polynomial Interpolation (estimation of points should be done before)
  - Spline Interpolation (estimation of points should be done before)

### Subtract Patterns

To subtract two patterns. Select the two pattens and the factor applied to pattern 2 in the subtract dialog and click on subtract to generate the subtracted pattern as a new data series. Auto scale will set automatically the factor so the subtracted pattern has no negative intensity in the 2-theta range specified.

![](.//Pictures/100000000000045E000002CD911A740189F63D0E.png)

### Rebinning

It will ask for initial 2-theta, final 2-theta and stepsize. Then it performs a rebinning of the data (by linear interpolations). A new series is generated.

### Convert to wavelength

Creates a new data series of the selected pattern converted to a new wavelength.

### Change X units

To change between 2-Theta, d-spacing, 1/dspacing<sup>2</sup>, Q. The correct wavelength needs to be assigned before (on the corresponding table cell). A new data series is generated.

### Sum selected patterns

Sum the selected patterns. The resulting pattern is created as a new data series.

### Fit peaks

Fit the peaks (from a peaks series generated from the *Find peaks* dialog) that are currently displayed on the screen with a pseudoVoigt and outputs the refined parameters. Otherwise a pseudoVoigt function can be manually added to the table and plotted. 

This fit peaks module is currently under development and may not be fully functional.

![](.//Pictures/100000000000046B000002B44D89DCCCC45D2E91.png)

## 5. Tools

### Compound database

Here a plain-text DB file can be loaded. By default it opens the file default.db (which is in the program folder) as the example one coming with *d1Dplot*. Once loaded, a compound can be selected to see the expected reflections on the pattern:

![](.//Pictures/10000000000005FF00000261670B1C492AA5C07E.png)

If either the *REF intensity* option on the main window or the *Show intensity* checkbox of Compound DB window is enabled, the vertical lines will be scaled according to the calculated intensity:

![](.//Pictures/10000000000005F60000026444E7F53D6CDEBB43.png)

Any selected compound may be edited from the fields on the right section of the window and clicking *apply changes* to update it or *add as new* to copy it as a new entry. Also new compounds may be added or removed by clicking *new* or *remove*. For new compound the information should be introduced. If the unit cell and space groups are known, the expected reflection positions can be calculated with *calc Refl* and the hkl list will be updated automatically. Alternatively, an HKL file or a CIF file can be imported. For CIF files, the hkl list (with calculated structure factors) will be automatically generated taking the cell parameters, symmetry and atom positions from the file. A confirmation window will show the information retrieved from the CIF to check for  correctness.

Alternatively you can edit manually the DB file. It is a simple self-explanatory text file and its format is explained in another section of this guide.

There is the possibility to search in the database by peak positions (from a peaks series) To search by peaks:

  - On the main window select the pattern with a peak series you want to use.
  - Click the button *search by peaks*. 
  - List will be updated by the best matching compounds (with respective residuals)
  - Click on the compounds to see the peak positions on top of your pattern to check if there is a good match.

![](.//Pictures/1000000000000623000002794486281E2580477B.png)

*Note*: The purpose of this database system in *d1Dplot* is to allow you (the user of the program) creating your own database with your choice of compounds (e.g. the family of compounds you are working with as possible candidates for phase identification). There are several
compound databases where you can find X-ray diffraction information, including *d*-spacings to introduce to your *d1Dplot* database. These databases can be proprietary ([ICDD](http://www.icdd.com/), [ICSD](https://icsd.fiz-karlsruhe.de/), [CCDC](http://www.ccdc.cam.ac.uk/),...) so that you need to purchase a license, or free ([COD](http://www.crystallography.net/)). The author of
*d1Dplot* takes no responsibilities regarding where the final users of the program gets the X-ray diffraction information or its correctness. The default DB is a small selection of 60 compounds taken from different sources. Each entry contains the reference from where it has been taken (with the respective authors) which can be retrieved from the *reference* field on each entry of the database. If any of these entries should be removed (for whatever reason) please contact the author and they will be removed immediately.

## 5. Command line mode

From the command line the program can be launched with one or more files given as arguments to open them directly.

However several operations on patterns can be performed from the command line without opening the GUI if the option *-macro* is added as the first argument followed by:

  - The files to which operations will be performed (filenames, paths, etc...)
  - The operation that will be performed
  - Additional options to the operation.

(if the first argument is *-help* then the different options are listed)

The following operations are available:

  - **conv**   
    Individally convert entered patterns according to the OPTIONS supplied (change format, wavel, etc...)
  - **sum**   
    Sum the input patterns, additional OPTIONS will be applied on the result
  - **diff FACT [T2I T2F]**   
    In this case, first pattern on the list will act as background.It will be subtracted to all other files   
    The operation is: Patt - Fact*Background   
    Additional options will be applied on the resulting files   
    If FACT<0 automatic scaling will be performed using the range from T2I to T2F (T2I and T2F can be supplied only when FACT<0)
  - **rebin T2I STEP T2F**   
    Applies a rebinning on the input patterns according to T2I STEP T2F   
    Additional options may be applied on the resulting files

Which can can be combined with the following OPTIONS:

  - **out NAME**    
  NAME will be added as suffix to the output files when batch processing (before the extension).   
  For sum and diff options NAME will be the full output filename (without extension)
  - **xIn XUN**     
  Specify the input x units of the pattern(s) (XUN= 2Theta, d-spacing, 1/dsp2, Q) (def=2Theta)
  - **xOut XUN**    
  To change the x units of the pattern(s) (XUN= 2Theta, d-spacing, 1/dsp2, Q)
  - **fmtIn EXT**   
  Specify the input file format of the pattern(s) (EXT= DAT, XYE, GSA, XRDML,...) (def=autodetect)
  - **fmtOut EXT**  
  Output format of the pattern(s) (EXT= DAT, XYE, GSA, XRDML,...) (def=same as input)
  - **waveIn WL**   
  Wavelength (A) of the input pattern(s) (def= from header if available
  - **waveOut WL**  
  To change the wavelength of the pattern(s)


## 6. Supported formats

*Supported read extensions:*

  - DAT (XYE with header from ALBA MSPD beamline), XYE, XY, ASC, GSA, XRDML, FF (Free Format), D1P (**d**1**Dplot** profile), PRF, GR, REF, TXT (general columns file).

*Supported write extensions:*

  - DAT (XYE with header from ALBA MSPD beamline), XYE, ASC, GSA, XRDML, GR, FF, REF.

### DAT (ALBA), XYE, XY, ASC

These are ASCII files. DAT (ALBA) and XYE are a 3-column (2-theta,
Intensity, err) with comment lines at the beginning (they may start with \#, \!, / or $). XY and ASC are 2-column files (2-theta, intensity) and ASC does not contain any header.

```
# I(2Theta) vs. 2Theta : IsMon = [3362599, 3364412, 3364882] IsPos = [-2.0004, -2.99299999, ...
# imon 3362599 ixbo_timer 0.1 ixbfe_tot 4.66493951416e-08 ixbo_tot 2.3019625e-07 ixbhp_tot ...
#     7179
2.0263000E+00 7.7488743E+03 8.7492857E+01
2.0323000E+00 7.6388975E+03 8.8594582E+01
2.0383000E+00 7.5582480E+03 8.6729464E+01
2.0443000E+00 7.5894229E+03 8.6740810E+01
2.0503000E+00 7.8062298E+03 8.9039317E+01
2.0563000E+00 7.7854571E+03 8.7692645E+01
...
```

### GSA

GSAS Standard Powder Data File (Larson & Von Dreele, 1994).

### XRDML

Panalytical format (Degen, 2002).

### D1P
*d1Dplot* profile file which puts on the same file an experimental
pattern, a calculated one and the hkl positions of the fitted phases. The format is a header followed by the data in columns (2-theta, Yobs, Ycal, Ybkg) followed by blocks of hkl phases (columns 2-theta and hkl)

```
#d1Dplot pattern matching obs/calc/hkl data
name=LaB6_diamond.dat
cell=
sg=
wave=0.95313
zero=0.00000
units=2θ (º)
DATA
1.5603000e+00 2.1115000e+04 0.0000000e+00 0.0000000e+00
1.5663000e+00 2.0625000e+04 0.0000000e+00 0.0000000e+00
1.5723000e+00 2.0618000e+04 0.0000000e+00 0.0000000e+00
1.5783000e+00 2.0748000e+04 0.0000000e+00 0.0000000e+00
1.5843000e+00 2.1091000e+04 0.0000000e+00 0.0000000e+00
1.5903000e+00 2.0732000e+04 0.0000000e+00 0.0000000e+00
….
phase 1 (hkl)
1.2839900e+01 1 0 0
1.6368700e+01 0 1 1
1.6368700e+01 1 0 1
2.2348500e+01 1 1 0
2.4094900e+01 0 1 2
...
phase 2 (hkl)
1.3145900e+01 1 0 0
1.8640500e+01 1 1 0
2.2885500e+01 1 1 1
…
```

### PRF

FullProf (Rodríguez-Carvajal, 1993) file “profile” generated after a
refinement containing the observed, calculated and difference profiles.

### REF

Reference file. It contains the name and wavelength in the header and a list of 2theta values with relative intensity.

```
# name= Silicon 
# wavelength= 0.9531
17.485110 100.00
28.743340 64.31
33.841636 37.32
41.098093 9.58
44.978069 14.14
50.924815 19.38
```

### GR

G(r) output file from pdfgetx3 (Juhás, Davis, Farrow & Billinge, 2013).

## 7. References

Boultif, A., & Louër, D. (2004). Powder pattern indexing with the dichotomy method. *Journal of Applied Crystallography*, *37*(5), 724-731.

Brückner, S. (2000). Estimation of the background in powder diffraction patterns through a robust smoothing procedure. *Journal of Applied Crystallography*, *33*(3), 977-979.

Degen, T. (2002). XrdML, a new way to store (and exchange) X-ray powder diffraction measurement data. *arXiv preprint physics/0210067*.

Juhás, P., Davis, T., Farrow, C. L., & Billinge, S. J. (2013). PDFgetX3: a rapid and highly automatable program for processing powder diffraction data into total scattering pair distribution functions. *Journal of Applied Crystallography*, *46*(2), 560-566.

Larson, A. C., & Von Dreele, R. B. (1994). Gsas. *General Structure Analysis System. LANSCE, MS-H805, Los Alamos, New Mexico*.

Rodríguez-Carvajal, J. (1993). Recent advances in magnetic structure determination by neutron powder diffraction. *Physica B: Condensed Matter*, *192*(1-2), 55-69.

Vallcorba, O., Rius, J., Frontera, C., Peral, I., & Miravitlles, C. (2012). DAJUST: a suite of computer programs for pattern matching, space-group determination and intensity extraction from powder diffraction data. *Journal of Applied Crystallography*, *45*(4), 844-848.

Vallcorba, O., Rius, J., Frontera, C., & Miravitlles, C. (2012) TALP: a multisolution direct-space strategy for solving molecular crystals from powder diffraction data based on restrained least squares. *Journal of Applied Crystallography*, *45*(6), 1270-1277.

Vallcorba, O., Rius, J. (2019). d2Dplot: 2D X-ray diffraction data processing and analysis for through-the-substrate microdiffraction. *Journal of Applied Crystallography*, 52, 478-484.

## 8. Miscellaneous

### Release notes

*d1Dplot* started in 2017 as a complement to DAjust, TALP and *d2Dplot* for personal and development use. However, several users of the MSPD beamline in ALBA asked for the availability of the program after using its plotting capabilities and basic operations during their beamtime and this is why it is distributed (although it is at a development stage and it still contains some bugs and missing functionality...). 

Feedback to the author would be greatly appreciated.

*d1Dplot* is completely programmed with Java<sup>TM</sup>
([www.java.com](http://www.java.com/)) using OpenJDK version 11.0.9.1 (GNU General Public License, version 2, with the Classpath Exception: <https://openjdk.java.net/legal/gplv2+ce.html>). You may find Oracle's free, GPL-licensed, production-ready OpenJDK binaries necessary to run *d1Dplot* at <https://openjdk.java.net/>.

The following 3<sup>rd</sup> party libraries have been used:

  - MigLayout.
    [http://](http://www.miglayout.com/)[www.miglayout.com](http://www.miglayout.com/)   
    BSD license: <http://directory.fsf.org/wiki/License:BSD_4Clause>

  - Commons Math. <https://commons.apache.org/proper/commons-math>      
    Apache License: <http://www.apache.org/licenses/LICENSE-2.0>

  - Apache Batik. <https://xmlgraphics.apache.org/batik/>   
    Apache License: <http://www.apache.org/licenses/LICENSE-2.0>

(No changes on the source codes of these libraries have been made, you can download the source codes for these libraries at their respective websites).

### Contact information
<div style="text-align: center;">
<img src=".//Pictures/1000000100000320000001B78A3B14ECB5904E4F.png" width="200"/>    

**Oriol Vallcorba**   
ALBA Synchrotron Light Source - CELLS (www.cells.es)   
Carrer de la Llum 2-26, 08290 Cerdanyola del Vallès, Barcelona (Spain)   
Phone: +34 93 592 4363    
e-mail: ovallcorba@cells.es
</div>

### Conditions of use

From August 2022 the program is open source and licensed under the GPL-3.0 license \[<https://www.gnu.org/licenses/gpl-3.0.en.html>\].

Citation of the author/program/affiliation, e.g. O. Vallcorba & J. Rius. XRD data visualization, processing and analysis with d1Dplot and d2Dplot software packages. (doi:10.3390/IOCC\_2020-07311), would be greatly appreciated when this program helped to your work.

### Disclaimer

This software is distributed WITHOUT ANY WARRANTY. The authors (or their institutions) have no liabilities in respect of errors in the software, in the documentation and in any consequence of erroneous results or damages arising out of the use or inability to use this software. Use it at your own risk.

*d1Dplot* is programmed with Java™

### Acknowledgments

Thanks are due the Spanish "Ministerio de Ciencia e Innovación", to the "Generalitat the Catalunya" and to the ALBA Synchrotron for continued financial support.

Copyright © Oriol Vallcorba 2017
(document last revision on Aug 19, 2022)
