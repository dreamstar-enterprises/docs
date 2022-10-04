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

## FREQ.SIMPLE

### About

Calculates a simple frequency table of the values in a column.

Sorts in decending order of frequency outputs.

#### Inputs:

  - data - a single column of data

### Code

{% capture code %}
FREQ.SIMPLE = LAMBDA(data,
    LET(
        d, INDEX(data, , 1),
        u, UNIQUE(d),
        X, N(u = TRANSPOSE(d)),
        Y, SEQUENCE(ROWS(d), 1, 1, 0),
        mp, MMULT(X, Y),
        c, CHOOSE({1, 2}, u, mp),
        SORT(c, 2, -1)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}