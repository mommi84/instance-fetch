#!/usr/bin/bash
rm dbo-fungus-*

grep "<http://dbpedia.org/resource/Corticium_penicillatum>" InfectiousFungi.nt >> dbo-fungus-pos.nt
grep "<http://dbpedia.org/resource/Gibberella_fujikuroi>" InfectiousFungi.nt >> dbo-fungus-pos.nt
grep "<http://dbpedia.org/resource/Phakopsora_pachyrhizi>" InfectiousFungi.nt >> dbo-fungus-pos.nt

grep "<http://dbpedia.org/resource/Hysteropeltella>" NonInfectiousFungi.nt >> dbo-fungus-neg.nt
grep "<http://dbpedia.org/resource/Tarzetta>" NonInfectiousFungi.nt >> dbo-fungus-neg.nt
grep "<http://dbpedia.org/resource/Metus_(fungus)>" NonInfectiousFungi.nt >> dbo-fungus-neg.nt