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

## APIVOT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/apivot.1167774/page-2#posts){:target="_blank"}

### About

Completes the pivot table layout "cosmetics" with labels and grand totals calculations. 

Calls [PIVOT](../lambda-library/lambda-pivot.html).

#### Inputs:

  - r, c, v, fn - same arguments as the PIVOT function
  - [p] - pivot table name label, if omitted "PT"
  - [tc] - trailing column label, if omitted "GT"
  - [tr] - trailing row label, if omitted "GT")

#### More Info:

*NOTE*: If fn returns more than a single value, those values will be text joined.

### Code

{% capture code %}
APIVOT = LAMBDA(r, c, v, fn, [p], [tr], [tc],
    LET(
        ur, SORT(UNIQUE(r)),
        uc, TOROW(SORT(UNIQUE(c))),
        x, VSTACK(IF(p = "", "PT", p), ur, IF(tc = "", "GT", tc)),
        y, VSTACK(uc, PIVOT(r, c, v, fn), PIVOT(, c, v, fn)),
        z, VSTACK(IF(tr = "", "GT", tr), PIVOT(r, , v, fn), PIVOT(, , v, fn)),
        a, HSTACK(x, y, z),
        FILTER(FILTER(a, TAKE(a, 1) <> 0), TAKE(a, , 1) <> 0)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}