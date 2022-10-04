---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Descriptive Statistic & Basic Maths functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## MAPX

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/mp.1217166/){:target="_blank"}

### About

MAP Expander.

Expands all its array arguments into arrays so they have the same dimensions, and indexes them using "n".


#### Inputs:

   - n - array's index nr., 1, 2, 3 or 4
   - a - array
   - b - array
   - [c] - array, if omitted n can be 1 or 2.; not omitted n can be 1, 2, or 3
   - [d] - array, if omitted n can be 1, 2 or 3; not omitted n can be 1, 2, 3 or 4


### Code

{% capture code %}
MAPX = LAMBDA(n, a, b, [c], [d],
    LET(
        w, MAX(
            ROWS(a),
            ROWS(b),
            IFERROR(ROWS(c), 0),
            IFERROR(ROWS(d), 0)
        ),
        l, MAX(
            COLUMNS(a),
            COLUMNS(b),
            IFERROR(COLUMNS(c), 0),
            IFERROR(COLUMNS(d), 0)
        ),
        p, LAMBDA(x, IFNA(EXPAND(x, w, l), x)),
        SWITCH(n, 1, p(a), 2, p(b), 3, p(c), 4, p(d))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}