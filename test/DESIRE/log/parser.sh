#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Uso: $0 ARQUIVO.log"
    exit 1
fi

LOG="$1"

if [ ! -f "$LOG" ]; then
    echo "Arquivo não encontrado: $LOG"
    exit 1
fi

# limpando

INPUT="$LOG"

OUTPUT=$(mktemp /tmp/inAnalysis.XXXXXX.log)
#trap 'rm -f "$OUTPUT"' EXIT

grep -v \
    -e '^\[httpserver\]' \
    -e '^\[CHON\]' \
    -e '^\[JaCaMoLauncher\]' \
    -e '^\[Include\]' \
    -e '^\[AAMAS 2027\]' \
    "$INPUT" > "$OUTPUT"

LOG="$OUTPUT"



awk '

function reset_cycle() {
    sense_content = 0
    plan_content  = 0
    act_content   = 0
    sense_exact_step = 0
    plan_exact_empty = 0
    act_exact_empty  = 0
}

# ============================================================
# BDI CYCLE
# ============================================================

/^\[[^]]+\] BDI Cycle: Starting/ {
    phase = "NONE"
    reset_cycle()
    next
}


/^\[[^]]+\] BDI Cycle: Finishing/ {

    # Conta somente ciclos BDI completos.
    # Um ciclo iniciado mas não finalizado no fim do log é descartado.
    total_cycles++

    if (phase == "ACT" && act_exact_empty)
        empty_act_raw++

    # Ciclo completamente vazio:
    # nenhuma atividade em SENSE, PLAN ou ACT.
    if (!sense_content && !plan_content && !act_content)
        empty_cycles++

    phase = "NONE"
    next
}


# ============================================================
# SENSE
# ============================================================

/^\[[^]]+\] Start sense/ {
    phase = "SENSE"
    sense_exact_step = 1
    next
}


# Marcadores da percepção corporal.
# A presença dessas linhas NÃO significa que houve
# alteração perceptual.

/^\[[^]]+\] Body update percepts, starting\.\.\./ {
    if (phase == "SENSE" && sense_exact_step == 1)
        sense_exact_step = 2
    else if (phase == "SENSE")
        sense_exact_step = -1
    next
}


/^\[[^]]+\] Body update percepts, finished\.\.\./ {
    if (phase == "SENSE" && sense_exact_step == 2)
        sense_exact_step = 3
    else if (phase == "SENSE")
        sense_exact_step = -1
    next
}


# Qualquer outra linha dentro do SENSE quebra a sequência exata
# de SENSE vazio usada na validação com Perl.
# Não usamos next: a linha ainda precisa ser processada pelas
# regras abaixo (eventos, revisões de crenças etc.).
phase == "SENSE" &&
$0 !~ /^\[[^]]+\] Starting plan/ {
    sense_exact_step = -1
}


# ============================================================
# PLAN
#
# A chegada a "Starting plan" encerra o SENSE.
# Se nenhuma outra linha ocorreu durante o SENSE,
# então o processo SENSE foi vazio.
# ============================================================

/^\[[^]]+\] Starting plan/ {

    # SENSE vazio incorporado = sequência estrutural exata:
    # Start sense -> Body update starting -> Body update finished -> Starting plan
    if (sense_exact_step == 1 || sense_exact_step == 3)
        empty_sense_raw++
    else
        useful_sense++

    phase = "PLAN"
    plan_exact_empty = 1
    next
}


# ============================================================
# PLAN EMPTY DETECTION
# ============================================================

# PLAN vazio = "Starting plan" imediatamente seguido por
# "Starting act". Qualquer outra linha no meio invalida
# a sequência exata.
phase == "PLAN" &&
$0 !~ /^\[[^]]+\] Starting act/ {
    plan_exact_empty = 0
    act_exact_empty  = 0
}


# ============================================================
# ACT
# ============================================================

/^\[[^]]+\] Starting act/ {

    if (plan_exact_empty)
        empty_plan_raw++

    phase = "ACT"
    act_exact_empty = 1
    next
}


# ============================================================
# ACT EMPTY DETECTION
# ============================================================

# ACT vazio = "Starting act" imediatamente seguido por
# "BDI Cycle: Finishing". Qualquer outra linha no meio
# invalida a sequência exata.
phase == "ACT" &&
$0 !~ /^\[[^]]+\] BDI Cycle: Finishing/ {
    act_exact_empty = 0
}


# ============================================================
# BELIEF REVISION / PERCEPTUAL LOAD
# ============================================================

# ------------------------------------------------------------
# Added event +belief
#
# Conta eventos de adição de crença segundo a fase.
# NÃO conta achievement goals (+!goal).
# ------------------------------------------------------------

/^\[[^]]+\] Added event \+/ {

    # ignora eventos de achievement goal
    if ($0 ~ /Added event \+!/)
        next

    belief_add[phase]++

    if (phase == "SENSE")
        sense_content = 1

    if (phase == "PLAN")
        plan_content = 1

    if (phase == "ACT")
        act_content = 1

    next
}


# ------------------------------------------------------------
# Added event -belief
#
# Conta eventos de remoção de crença segundo a fase.
# NÃO conta achievement goals (-!goal).
# ------------------------------------------------------------

/^\[[^]]+\] Added event -/ {

    # ignora eventos de achievement goal
    if ($0 ~ /Added event -!/)
        next

    belief_del[phase]++

    if (phase == "SENSE")
        sense_content = 1

    if (phase == "PLAN")
        plan_content = 1

    if (phase == "ACT")
        act_content = 1

    next
}


# ------------------------------------------------------------
# BODY PERCEPTS
#
# Alterações produzidas pela percepção corporal.
# São contadas separadamente dos Added event porque aparecem
# explicitamente como +myBody:: / -myBody:: no log.
# ------------------------------------------------------------

/^\[[^]]+\]  \+myBody::/ {

    body_add[phase]++

    if (phase == "SENSE")
        sense_content = 1

    if (phase == "PLAN")
        plan_content = 1

    if (phase == "ACT")
        act_content = 1

    next
}


/^\[[^]]+\]  -myBody::/ {

    body_del[phase]++

    if (phase == "SENSE")
        sense_content = 1

    if (phase == "PLAN")
        plan_content = 1

    if (phase == "ACT")
        act_content = 1

    next
}

# ============================================================
# DELIBERATION
# ============================================================

/^\[[^]]+\] Selected event / {

    if (phase == "PLAN") {
        selected_events++
        plan_content = 1
    }

    next
}

# Context-condition outcomes. Internal-action traces are kept out
# of CE so that succeeded/failed represent logical context tests.
phase == "PLAN" && /\| .* succeeded[[:space:]]+--/ {
    context_succeeded++
    plan_content = 1
    next
}

phase == "PLAN" && /\| .* failed[[:space:]]+--/ {
    if ($0 !~ /internal action /) {
        context_failed++
        plan_content = 1
    }
    next
}

# Belief/rule search during context applicability checking.
phase == "PLAN" && /\| for .*belief .* is an option[[:space:]]+--/ {
    belief_options++
    plan_content = 1
    next
}

phase == "PLAN" && /\| no candidate belief for / {
    no_candidate_belief++
    plan_content = 1
    next
}

phase == "PLAN" && /\| for .*rule .* is an option[[:space:]]+--/ {
    rule_options++
    plan_content = 1
    next
}

phase == "PLAN" && /\| no candidate rule for / {
    no_candidate_rule++
    plan_content = 1
    next
}


# ============================================================
# EXECUTION
# ============================================================

/^\[[^]]+\] Selected intention / {

    selected_intentions++
    act_content = 1

    next
}


# ============================================================
# QUALQUER OUTRA LINHA DURANTE SENSE
#
# Se chegamos até aqui e ainda estamos em SENSE,
# a linha não é um simples marcador estrutural.
#
# Portanto houve conteúdo perceptual.
#
# Isso captura tanto:
#   - percepção ambiental
#   - percepção corporal
# ============================================================

{
    if (phase == "SENSE") {
        sense_content = 1
        sense_exact_step = -1
    }
}


# ============================================================
# RESULT
# ============================================================

END {

    # --------------------------------------------------------
    # BDI
    # --------------------------------------------------------

    active_cycles = total_cycles - empty_cycles


    # --------------------------------------------------------
    # SENSE
    #
    # Os ciclos BDI completamente vazios também possuem
    # SENSE vazio. Eles são retirados da análise do SENSE.
    # --------------------------------------------------------

    sense_total  = active_cycles
    sense_empty  = empty_sense_raw - empty_cycles
    sense_useful = sense_total - sense_empty

    # PLAN: os BDI completamente vazios também possuem PLAN vazio.
    plan_total  = active_cycles
    plan_empty  = empty_plan_raw - empty_cycles
    plan_useful = plan_total - plan_empty

    # ACT: os BDI completamente vazios também possuem ACT vazio.
    act_total  = active_cycles
    act_empty  = empty_act_raw - empty_cycles
    act_useful = act_total - act_empty


    # --------------------------------------------------------
    # OUTPUT
    # --------------------------------------------------------

    print ""
    print "========================================"
    print " Jason Log Parser"
    print "========================================"
    print ""


    print "BDI CYCLES"
    print "----------------------------------------"

    printf "%-30s %d\n", "Total:",  total_cycles
    printf "%-30s %d\n", "Empty:",  empty_cycles
    printf "%-30s %d\n", "Active:", active_cycles


    print ""
    print "SENSE"
    print "----------------------------------------"

    printf "%-30s %d\n", "Total:",  sense_total
    printf "%-30s %d\n", "Empty:",  sense_empty
    printf "%-30s %d\n", "Useful:", sense_useful



    print ""
    print "PLAN"
    print "----------------------------------------"

    printf "%-30s %d\n", "Total:",  plan_total
    printf "%-30s %d\n", "Empty:",  plan_empty
    printf "%-30s %d\n", "Useful:", plan_useful


    print ""
    print "ACT"
    print "----------------------------------------"

    printf "%-30s %d\n", "Total:",  act_total
    printf "%-30s %d\n", "Empty:",  act_empty
    printf "%-30s %d\n", "Useful:", act_useful


    print ""
    print "PERCEPTUAL LOAD"
    print "----------------------------------------"

    printf "%-30s %d\n",
        "Environment additions:",
        belief_add["SENSE"]

    printf "%-30s %d\n",
        "Environment removals:",
        belief_del["SENSE"]

    printf "%-30s %d\n",
        "Body additions:",
        body_add["SENSE"]

    printf "%-30s %d\n",
        "Body removals:",
        body_del["SENSE"]

    perceptual_add = belief_add["SENSE"] + body_add["SENSE"]

    perceptual_del = belief_del["SENSE"] + body_del["SENSE"]

    perceptual_total = perceptual_add + perceptual_del

    printf "%-30s %d\n",
        "Total additions:",
        perceptual_add

    printf "%-30s %d\n",
        "Total removals:",
        perceptual_del

    printf "%-30s %d\n",
        "Total revisions:",
        perceptual_total





    print ""
    print "DELIBERATION"
    print "----------------------------------------"

    context_total = context_succeeded + context_failed
    belief_queries = belief_options + no_candidate_belief
    rule_queries = rule_options + no_candidate_rule

    printf "%-30s %d\n",
        "Selected-event traces (SET):",
        selected_events

    printf "%-30s %d\n",
        "Context succeeded:",
        context_succeeded

    printf "%-30s %d\n",
        "Context failed:",
        context_failed

    printf "%-30s %d\n",
        "Context evaluations (CE):",
        context_total

    if (context_total > 0)
        printf "%-30s %.1f%%\n",
            "Context failure rate:",
            (100.0 * context_failed / context_total)
    else
        printf "%-30s %.1f%%\n",
            "Context failure rate:", 0

    printf "%-30s %d\n",
        "Belief candidates:",
        belief_options

    printf "%-30s %d\n",
        "Belief query failures:",
        no_candidate_belief

    printf "%-30s %d\n",
        "Belief queries (BQ):",
        belief_queries

    printf "%-30s %d\n",
        "Rule candidates:",
        rule_options

    printf "%-30s %d\n",
        "Rule query failures:",
        no_candidate_rule

    printf "%-30s %d\n",
        "Rule queries (RQ):",
        rule_queries


    print ""
    print "EXECUTION"
    print "----------------------------------------"

    execution_revisions = belief_add["ACT"] + belief_del["ACT"]

    printf "%-30s %d\n",
        "Execution steps (ES):",
        act_useful

    printf "%-30s %d\n",
        "Belief additions:",
        belief_add["ACT"]

    printf "%-30s %d\n",
        "Belief removals:",
        belief_del["ACT"]

    printf "%-30s %d\n",
        "Belief revisions (BR):",
        execution_revisions


    print ""
}

' "$LOG"