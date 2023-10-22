$services = Read-Host "What services do you want to build? [persistence/legality/controller/chess/all]"

if ($services -like "*persistence*")
{
    docker compose -f "docker-compose-mongodb.yaml" -f "docker-compose.yaml" build $services
    docker compose -f "docker-compose-postgres.yaml" -f "docker-compose.yaml" build "persistence"
    docker compose -f "docker-compose-sqlite.yaml" -f "docker-compose.yaml" build "persistence"
}
elseif ($services -like "*all*") {
    docker compose -f "docker-compose-mongodb.yaml" -f "docker-compose.yaml" build persistence legality controller chess
}
else {
    docker compose -f "docker-compose-sqlite.yaml" -f "docker-compose.yaml" build $services
}
