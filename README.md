# i-read-slowly

I like having library books, but I often read too slowly to finish a
batch before they expire. You can renew loans online, however, as long
as no one else has put in a request. This is a tiny program that
automates renewing all your checked out items.

Note that there's a limit to how many times you can renew at item (5,
I think?), and so you should only renew often if you think the book is
in demand and will have a hold placed. I renew once a week.

## Usage

It takes your 14-digit NYPL barcode and 4-digit pin (you might have to
sign up for online access on their [website] (http://nypl.org)).

$ lein run -help
Renew all checked out items for NYPL account.
Options
  --barcode, -b <arg>  NYPL 14 digit barcode 
  --pin, -p <arg>      4 digit PIN 

To run the program from cron, you can build an uberjar with
[leiningen] (https://github.com/technomancy/leiningen) and run
something like:

java -jar i-read-slowly-1.0-standalone.jar -b <barcode> -p <pin>

The program prints your checked-out items page after it tries to renew
them, which shows if it was successful or if you've reached your renew
limit. To get this, I just pipe the output of the program to main
(because I don't actually get the mail generated from cron output):

java -jar i-read-slowly-1.0-standalone.jar -b <barcode> -p <pin> | mail -s "i-read-slowly" you@wherever

## License

Copyright (C) 2011 WTFPL
