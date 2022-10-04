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

## PIVOT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/apivot.1167774/page-2#posts){:target="_blank"}

### About

Does "inner" array calculations of the Pivot Table. Used as a Helper function of [APIVOT](../lambda-library/lambda-apivot.html).

#### Inputs:

  - r - 'rows' column of an array or table.
  - c - 'columns' column of an array or table
  - v - 'values' column of an array or table
  - fn - lambda helper function argument (also known as a 'thunk'): LAMBDA(x,function(x))

#### More Info:

*NOTE*: If fn returns more than a single value, those values will be text joined.

### Code

{% capture code %}
PIVOT = LAMBDA(r, c, v, fn,
    LET(
        d, ",",
        ur, SORT(UNIQUE(r)),
        uc, SORT(UNIQUE(c)),
        w, ROWS(ur),
        l, ROWS(uc),
        MAKEARRAY(
            w,
            l,
            LAMBDA(y, x,
                LET(
                    a, INDEX(ur, y),
                    b, INDEX(uc, x),
                    i, IF((a = r) * (b = c), v, ""),
                    f, FILTER(i, i <> ""),
                    fx, IF(ISERR(SUM(f)), "", IFERROR(fn(f), "")),
                    IF(COUNTA(fx) > 1, TEXTJOIN(d, , fx), fx)
                )
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}