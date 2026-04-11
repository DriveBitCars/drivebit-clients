#!/usr/bin/env bash
set -euo pipefail

SERVICE=/etc/systemd/system/drivebit-nr-hugepages-zero.service
TIMER=/etc/systemd/system/drivebit-nr-hugepages-zero.timer

if [[ "$(id -u)" -ne 0 ]]; then
  echo "Run as root (on the VPS)." >&2
  exit 1
fi

cat >"$SERVICE" <<'UNIT'
[Unit]
Description=Drivebit: keep vm.nr_hugepages at 0 on small VPS
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/sbin/sysctl -w vm.nr_hugepages=0
RemainAfterExit=yes
UNIT

cat >"$TIMER" <<'UNIT'
[Unit]
Description=Drivebit: periodic vm.nr_hugepages=0 (catches late allocators)

[Timer]
OnBootSec=3min
OnCalendar=*-*-* 00,08,16:30
Persistent=true
AccuracySec=1min
RandomizedDelaySec=3min

[Install]
WantedBy=timers.target
UNIT

systemctl daemon-reload
systemctl enable --now drivebit-nr-hugepages-zero.timer
systemctl start drivebit-nr-hugepages-zero.service

echo "Status:"
systemctl is-enabled drivebit-nr-hugepages-zero.timer
systemctl is-active drivebit-nr-hugepages-zero.timer
sysctl vm.nr_hugepages
