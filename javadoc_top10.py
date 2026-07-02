import re
from pathlib import Path

KERNEL = Path(__file__).parent / "openjpa-kernel/src/main/java/org/apache/openjpa/kernel"
EVENT  = Path(__file__).parent / "openjpa-kernel/src/main/java/org/apache/openjpa/event"
LIB    = Path(__file__).parent / "openjpa-lib/src/main/java/org/apache/openjpa/lib/util"

# Ordine coerente con la dichiarazione nel sorgente:
# BrokerImpl implements Broker, FindCallbacks, Cloneable, Serializable
# Broker extends Synchronization, Closeable, StoreContext, ConnectionRetainModes,
#     DetachState, LockLevels, RestoreState, AutoClear, AutoDetach, CallbackModes
# (Synchronization, Cloneable, Serializable esclusi: non appartengono al progetto OpenJPA)
FILES = [
    (KERNEL / "BrokerImpl.java",           "BrokerImpl"),
    (KERNEL / "Broker.java",               "Broker"),
    (LIB    / "Closeable.java",            "Closeable"),
    (KERNEL / "StoreContext.java",         "StoreContext"),
    (KERNEL / "ConnectionRetainModes.java","ConnectionRetainModes"),
    (KERNEL / "DetachState.java",          "DetachState"),
    (KERNEL / "LockLevels.java",           "LockLevels"),
    (KERNEL / "RestoreState.java",         "RestoreState"),
    (KERNEL / "AutoClear.java",            "AutoClear"),
    (KERNEL / "AutoDetach.java",           "AutoDetach"),
    (EVENT  / "CallbackModes.java",        "CallbackModes"),
    (KERNEL / "FindCallbacks.java",        "FindCallbacks"),
]

def extract_methods(path):
    lines = path.read_text().splitlines()
    results = []
    i = 0
    while i < len(lines):
        if re.search(r'/\*\*', lines[i]):
            javadoc_start = i
            javadoc_lines = 0
            while i < len(lines):
                javadoc_lines += 1
                if re.search(r'\*/', lines[i]):
                    break
                i += 1
            javadoc_end = i
            j = javadoc_end + 1
            while j < len(lines) and re.match(r'^\s*(@|\s*$)', lines[j]):
                j += 1
            if j < len(lines):
                # Accumula righe finché la dichiarazione non si chiude (';' o '{'),
                # per gestire firme di metodo che si estendono su più righe.
                sig_lines = []
                k = j
                while k < len(lines) and k < j + 6:
                    sig_lines.append(lines[k])
                    if lines[k].strip().endswith((';', '{')):
                        break
                    k += 1
                sig = ' '.join(s.strip() for s in sig_lines)
                if '(' in sig and ')' in sig and not re.match(r'(public\s+)?(class|interface|enum)', sig):
                    sig = re.sub(r'\s+', ' ', sig).rstrip('{;').strip()
                    if len(sig) > 100:
                        sig = sig[:97] + '...'
                    results.append((javadoc_lines, sig, javadoc_start + 1))
            i = j
        else:
            i += 1
    results.sort(key=lambda x: -x[0])
    return results[:10]

for path, label in FILES:
    if not path.exists():
        print(f"\n{'='*70}")
        print(f"{label} ({path.name}) — FILE NON TROVATO")
        continue
    methods = extract_methods(path)
    print(f"\n{'='*70}")
    print(f"{label}  ({path.name})")
    print(f"{'='*70}")
    if not methods:
        print("  (nessun metodo con Javadoc trovato)")
        continue
    for javadoc_lines, sig, lineno in methods:
        print(f"  [{javadoc_lines:3d} righe Javadoc | riga {lineno:4d}]  {sig}")
