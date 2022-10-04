---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Combinatronic functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ARRANGEMENTS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/arrangements.1179396/){:target="_blank"}

### About

Calls [AFLAT](../lambda-library/lambda-aflat.html).

#### Inputs:

  - a - array
  - t - type argument,"pa", permutations with repetitions; "p" permutations w/o repetititions; "ca", combinations with repetitions; "c", combinations w/o repetitions
  - c - number_chosen

#### More Info:

1. PERMUTATIONA Returns the number of permutations for a given number of objects (with repetitions) that can be selected from the total objects.
    - PERMUTATIONA(number, number-chosen); "c" (number chosen, nc) can be >= n (number of elements/objects); order is important; PA=n^nc

2. PERMUT Returns the number of permutations for a given number of objects (no repetitions) that can be selected from the total objects.
    - PERMUT(number, number_chosen); if nc > n returns #NUM! error; also called arrangements; order is important; P=n!/(n-nc)!

3. COMBINA Returns the number of combinations (with repetitions) for a given number of items.
    - COMBINA(number, number_chosen); nc can be > n; order is not important; CA=(n+nc-1)!/(nc!*(n-1)!)

4. COMBIN Returns the number of combinations (no repetitions) for a given number of items.
    - COMBIN(number, number_chosen); if nc > n returns #NUM! error; order is not important; C=P/nc! or C=n!/(nc!*(n-nc)!)

{{site.data.alerts.important}}
\\
As this function includes some ***recursive*** helper functions, it can be particularly resource intensive on Excel (depending on the arguments you use). 

For example, on a set of 7 numbers, the formula *= ARRANGEMENTS({1,2,3,4,5,6,7}, "c", 7)*, which uses the two recursive functions T_P, and T_CA, crashes my Excel. (It apparently works for others, though.)

Therefore, I would recommend, as a final step, copying and pasting the results of the function (once you get them) as 'values' (via 'paste special'). This will prevent the workbook from needing to re-calculate the cell formula everytime something in the workbook changes.
{{site.data.alerts.end}}

### Code

{% capture code %}
// Helper tool Lambdas
T_PA = LAMBDA(n, c, MOD(ROUNDUP(SEQUENCE(n ^ c) / n ^ (c - SEQUENCE(, c)), 0) - 1, n) + 1);
T_P = LAMBDA(a, [ai], [i],
    LET(
        n, COLUMNS(a),
        j, IF(i = "", n, i),
        x, INDEX(a, , j),
        IF(j = 0, FILTER(a, MMULT(ai, SEQUENCE(n) ^ 0) = n), T_P(a, ai + (x = a), j - 1))
    )
);
T_CA LAMBDA(a, [ai], [i],
    LET(
        n, COLUMNS(a),
        j, IF(i = "", 1, i),
        aj, IF(ai = "", 1, ai),
        x, INDEX(a, , j),
        IF(j = n, FILTER(a, aj), T_CA(a, aj * (x <= INDEX(a, , j + 1)), j + 1))
    )
);

ARRANGEMENTS = LAMBDA(a, t, c,
    IF(
        AND(t <> {"p", "pa", "c", "ca"}),
        "check type",
        LET(
            k, MAX(1, c),
            x, AFLAT(a),
            n, ROWS(x),
            IF(
                AND(OR(t = {"p", "c"}), k > n),
                "nr chosen > n !!!",
                LET(
                    y, T_PA(n, k),
                    SWITCH(
                        t,
                        "pa",
                        INDEX(x, y),
                        "p",
                        INDEX(x, T_P(y)),
                        "ca",
                        INDEX(x, T_CA(y)),
                        "c",
                        LET(z, T_P(y), w, T_CA(z), INDEX(x, w))
                    )
                )
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}
