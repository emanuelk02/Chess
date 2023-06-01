echo "What database do you want? [sqlite, postgres, mongodb]"
read db
# Start docker-compose up depending on the database
if [ $db = "sqlite" ]; then
    docker compose -f docker-compose-mongodb.yaml -f docker-compose.yaml up
elif [ $db = "postgres" ]; then
    docker compose -f docker-compose-postgres.yaml -f docker-compose.yaml up
elif [ $db = "mongodb" ]; then
    docker compose -f docker-compose-sqlite.yaml -f docker-compose.yaml up
else
    echo "Invalid database"
fi