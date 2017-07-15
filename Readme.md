# Readme.md

## Author
Cyrus Chan, c04022004@gmail.com

## Background
This is a mini project that is inspired by the need to read offline in a kindle. As it can auto re-arrange pdf contend, I did not spend time to refine the look of the out put pdf. If you want to do that, you can change it in `HackerNewsDownloader.DownloadThread.run()`.

## Dependency
This program depends on `wkhtmltopdf` to convert webpages to pdf.
you can find it [here](https://wkhtmltopdf.org).
The `web2pdfPath` in `HackerNewsDownloader.java` may have to be changed according to you computer enviroment.
This java program uses Json library from [here](https://mvnrepository.com/artifact/org.json/json/20170516) and [here](https://github.com/stleary/JSON-java).

## Usage
After compilation, invoke in the console,
`java HackerNewsUI { # of threads for multithreading }`
and follow the GUI.
You may have to change 

## Known Issues
1. The `web2pdfPath` in `HackerNewsDownloader.java` may have to be changed according to you computer enviroment.
2. The download function is not working perfectly, some files my be left out, or the progress is stuck at a certain level until timeout.
3. The timer function is not thoroughly tested. A more relable way is by the `do ot manually` button.

## Credits
I’d like to thank the following sources:<p>
[wkhtmltopdf.org](https://wkhtmltopdf.org)<p>
[stleary](https://github.com/stleary/JSON-java)<p>
[HackerNews](https://github.com/HackerNews/API)<p>