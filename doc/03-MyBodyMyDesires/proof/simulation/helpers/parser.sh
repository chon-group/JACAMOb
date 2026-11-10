#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Uso: $0 <diretorio>"
    exit 1
fi

DIR="$1"

if [ ! -d "$DIR" ]; then
    echo "Diretório não encontrado: $DIR"
    exit 1
fi

for LOG in "$DIR"/*.log; do

    [ -f "$LOG" ] || continue

    echo "========================================"
    echo "Arquivo: $LOG"
    echo "========================================"

    echo "TEMPO DE EXECUÇÃO" 
    echo -n "INICIO: " 
    echo $(cat $LOG | grep 'event +!startClean')
    echo -n "FIM: " 
    echo $(cat $LOG | grep 'I finished!')
    echo

    echo "SENSE"
    echo -n " Eventos de adição de crença: "
    grep -E '\[(rosie|bob)\] Added event \+[^!]|\[(rosie|bob)\][[:space:]]+\+myBody::' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Eventos de adição de objetivo: "
    grep -E '\[(rosie|bob)\] Added event \+!' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo "PLAN"
    echo -n " Total de seleções de eventos: "
    grep -E '\[(rosie|bob)\] Selected event ' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Seleções de eventos de crença: "
    grep -E '\[(rosie|bob)\] Selected event [+-][^!]' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Seleções de eventos de objetivo: "
    grep -E '\[(rosie|bob)\] Selected event [+-]!' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Total de registros de avaliação de contexto: "
    grep -E '\[(rosie|bob)\].*\| .*(succeeded|is an option|is NOT an option|failed)' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Avaliações de contexto com sucesso: "
    grep -E '\[(rosie|bob)\].*\| .*(succeeded|is an option)' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo -n " Avaliações de contexto com falha: "
    grep -E '\[(rosie|bob)\].*\| .*(failed|is NOT an option)' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo "ACT"
    echo -n " Seleções de intenções: "
    grep -E '\[(rosie|bob)\] Selected intention intention ' "$LOG" | nl | tail -1 | xargs | cut -d " " -f 1

    echo 
    echo -n "RESULTADO: "
    grep -q 'batteryLevel(0)' $LOG && echo "FALHA" || echo "SUCESSO"
    
done