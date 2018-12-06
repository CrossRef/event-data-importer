# Event Data Importer

Tool for manually importing data into Event Data. 

## Scholix

For converting Scholix link packages into Events.

From a newline-delimited file of NDJSON-serialized Scholix objects, produce a corresponding file of newline-separated Events. In both the input and the output files newline characters within the JSON-LD message should be encoded with escape sequence, not represented as literals.

    lein run scholix-file «input-file» «output-file»

Or to run an entire directory:

    lein run scholix-dir «input-dir» «output-dir»

Once this is done, you can use the Upload task to send them into the Bus.

## Upload

Upload a NDJSON file of Events to the Event Bus. 

    lein run upload «input-files»
    
If you're using Bash you can take advantage of path-expansion:

    lein run upload /tmp/xyz/*

This command requires the following Environment variables to be set:

 - `GLOBAL_EVENT_BUS_BASE` - This can be the production or staging Event Bus.
 - `GLOBAL_JWT_SECRETS` - Meaning that the appropriate authorization is created for any source. 
 
## Experimentation

If you are testing out a new import, you should use the Staging configuration variables. e.g.

    GLOBAL_EVENT_BUS_BASE=https://bus-staging.eventdata.crossref.org/ GLOBAL_JWT_SECRETS=TEST lein run upload /tmp/xyz/*

## License

Copyright © 2018 Crossref

Distributed under the The MIT License (MIT).
