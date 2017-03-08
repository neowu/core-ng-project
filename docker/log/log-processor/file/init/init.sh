!#/bin/sh
curl -XPUT "http://elasticsearch:9200/_template/action" -d '@action-index-template.json'
curl -XPUT "http://elasticsearch:9200/_template/trace" -d '@trace-index-template.json'
curl -XPUT "http://elasticsearch:9200/_template/stat" -d '@stat-index-template.json'