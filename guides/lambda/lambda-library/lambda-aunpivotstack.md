---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## AUNPIVOTSTACK

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/insrws.1217485/page-2#posts){:target="_blank"}

### About

Unpivots the main headers of a table, and stacks this together with its sub headers.

Calls [ASTACK](../lambda-library/lambda-astack.html).

#### Inputs:

  - tb - the table, with headers, whose main header needs be unpivoted, and then stacked
  - ht - text value to give to main header

### Code

{% capture code %}
AUNPIVOTSTACK = LAMBDA(tb, ht,
    LET(
        mh, CHOOSEROWS(tb, 1),
        mhf, FILTER(mh, mh <> ""),
        sh, CHOOSEROWS(tb, 2),
        shu, UNIQUE(sh, TRUE),
        k, ASTACK(DROP(tb, 2), COLUMNS(shu)),
        f, TAKE(k, , 1),
        i, QUOTIENT(SEQUENCE(ROWS(k)) - 1, ROWS(tb) - 2) + 1,
        g, HSTACK(INDEX(mhf, i), k),
        l, FILTER(g, f <> ""),
        tbh, HSTACK(ht, shu),
        VSTACK(tbh, l)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}