#!/bin/bash

SSH_KEY="$(dirname "$0")/id_rsa_api_drivebit"
SSH_USER="user1"
SSH_HOST="api.drivebit.ru"
CONTAINER_NAME="drivebit-drivebitbackend-1"

if [ ! -f "$SSH_KEY" ]; then
    echo "Error: SSH key not found at $SSH_KEY"
    exit 1
fi

chmod 600 "$SSH_KEY"

if [ "$1" == "-f" ] || [ "$1" == "--follow" ]; then
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs -f $CONTAINER_NAME"
elif [ "$1" == "-n" ] || [ "$1" == "--lines" ]; then
    LINES=${2:-100}
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs --tail $LINES $CONTAINER_NAME"
else
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "$SSH_USER@$SSH_HOST" "docker logs --tail 100 $CONTAINER_NAME"
fi

