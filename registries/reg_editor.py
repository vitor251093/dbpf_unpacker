#!/usr/bin/env python3

import argparse
import os
import sys
import difflib
from collections import Counter


def read_file(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return [line.strip() for line in file if line.strip()]
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        sys.exit(1)
    except Exception as e:
        print(f"Error reading file '{file_path}': {e}")
        sys.exit(1)


def write_file(file_path, names):
    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            for name in sorted(names):
                file.write(f"{name}\n")
        print(f"File '{file_path}' successfully updated.")
    except Exception as e:
        print(f"Error writing to file '{file_path}': {e}")
        sys.exit(1)


def find_similar_names(name, name_list, threshold=0.8):
    matches = difflib.get_close_matches(name, name_list, n=3, cutoff=threshold)
    return [m for m in matches if m != name]


def deduplicate_input(names):
    name_counts = Counter(names)
    duplicates = {name: count for name, count in name_counts.items() if count > 1}
    unique_names = list(name_counts.keys())
    
    return unique_names, duplicates


def process_additions(registry_names, new_names):
    to_add = []
    duplicates = []
    similar = {}
    
    registry_set = set(registry_names)
    
    for name in new_names:
        if name in registry_set:
            duplicates.append(name)
        else:
            to_add.append(name)
            
            similar_names = find_similar_names(name, registry_names)
            if similar_names:
                similar[name] = similar_names
    
    return to_add, duplicates, similar


def confirm_changes(to_add, duplicates, similar, input_duplicates=None):
    if not to_add and not duplicates and not similar and not input_duplicates:
        print("No changes to make.")
        return False
    
    print("\n===== CHANGES SUMMARY =====")
    
    if input_duplicates:
        print(f"\n{len(input_duplicates)} name(s) with duplicates in input file:")
        for name, count in sorted(input_duplicates.items()):
            print(f"- {name} [{count}] ignored {count-1} repetitions")
    
    if to_add:
        print(f"\n{len(to_add)} name(s) to be added:")
        for name in sorted(to_add):
            print(f"+ {name}")
    
    if duplicates:
        print(f"\n{len(duplicates)} duplicate name(s) (will be ignored):")
        for name in sorted(duplicates):
            print(f"- {name}")
    
    if similar:
        print(f"\n{len(similar)} name(s) with possible similarities:")
        for name, matches in sorted(similar.items()):
            print(f"~ {name} (similar to: {', '.join(matches)})")
    
    print("\nApply these changes? (y/n): ", end="")
    response = input().lower()
    return response in ('y', 'yes')


def main():
    parser = argparse.ArgumentParser(
        description="Registry Editor - Tool for managing name lists with duplicate detection", 
        epilog="Example: python registryEditor.py main_registry.txt --add new_names.txt"
    )
    
    parser.add_argument("registry", help="Main registry file")
    parser.add_argument("--add", dest="add_file", help="File with new names to add")
    parser.add_argument("--backup", action="store_true", help="Create backup of registry before modifying")
    parser.add_argument("--deduplicate", "-d", action="store_true", help="Deduplicate input file before processing")
    
    args = parser.parse_args()
    
    if not args.add_file:
        parser.print_help()
        sys.exit(1)
    
    print(f"Reading registry file: {args.registry}")
    registry_names = read_file(args.registry)
    print(f"Found {len(registry_names)} names in registry.")
    
    print(f"Reading new names file: {args.add_file}")
    new_names_raw = read_file(args.add_file)
    print(f"Found {len(new_names_raw)} names in addition file.")
    
    input_duplicates = None
    if args.deduplicate:
        new_names, input_duplicates = deduplicate_input(new_names_raw)
        print(f"Deduplicated input file: {len(new_names_raw)} names → {len(new_names)} unique names")
    else:
        new_names = new_names_raw
    
    to_add, duplicates, similar = process_additions(registry_names, new_names)
    
    if confirm_changes(to_add, duplicates, similar, input_duplicates):
        if args.backup:
            backup_file = f"{args.registry}.bak"
            write_file(backup_file, registry_names)
            print(f"Backup created: {backup_file}")
        
        updated_registry = sorted(set(registry_names) | set(to_add))
        write_file(args.registry, updated_registry)
        
        print("\n===== STATISTICS =====")
        print(f"Total names in original registry: {len(registry_names)}")
        if args.deduplicate:
            print(f"Input file duplicates removed: {len(new_names_raw) - len(new_names)}")
        print(f"Names added: {len(to_add)}")
        print(f"Duplicates ignored: {len(duplicates)}")
        print(f"Total names in updated registry: {len(updated_registry)}")
    else:
        print("Operation cancelled by user.")


if __name__ == "__main__":
    main()