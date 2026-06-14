#!/usr/bin/env python3
import xml.etree.ElementTree as ET
import sys

tree = ET.parse('app/build/reports/jacoco/jacocoUnitTestReport/jacocoUnitTestReport.xml')
root = tree.getroot()
counters = root.findall('.//counter[@type="INSTRUCTION"]')
missed = sum(int(c.get('missed', 0)) for c in counters)
covered = sum(int(c.get('covered', 0)) for c in counters)
total = missed + covered
pct = covered * 100.0 / total if total > 0 else 0.0
print(f'Coverage: {covered}/{total} = {pct:.1f}%')
if pct < 95.0:
    print('FAIL: Coverage below 95%')
    sys.exit(1)
print('PASS: Coverage >= 95%')
