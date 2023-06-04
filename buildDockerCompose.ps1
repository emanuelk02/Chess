$services = Read-Host "What services do you want to build? [persistence, legality, controller, chess]"

if ($services -like "*persistence*")
{
    $db = Read-Host "What database do you want? [sqlite, postgres, mongodb]"
    docker compose -f "docker-compose-$db.yaml" -f "docker-compose.yaml" build "$services"
}
else {
    docker compose -f "docker-compose-sqlite.yaml" -f "docker-compose.yaml" build "$services"
}
