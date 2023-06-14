$db = Read-Host "What database do you want? [sqlite, postgres, mongodb]"
docker compose -f "docker-compose-$db.yaml" -f "docker-compose.yaml" up
