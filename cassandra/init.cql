CREATE KEYSPACE IF NOT EXISTS origin WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE IF NOT EXISTS origin.sentences ( key varchar,sentence varchar, PRIMARY KEY (key) );

CREATE CUSTOM INDEX search_index ON origin.sentences (sentence) USING 'org.apache.cassandra.index.sasi.SASIIndex'
WITH OPTIONS = {
'mode': 'CONTAINS',
'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.NonTokenizingAnalyzer',
'case_sensitive': 'false'};