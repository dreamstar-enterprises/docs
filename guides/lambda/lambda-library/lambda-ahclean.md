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

## AHCLEAN

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/ahclean.1179924/){:target="_blank"}

### About

Array Horizontal Clean, replaces errors with null strings and filters all the rows depending on nr. of blnks/null strings on each row. 

#### Inputs:

  - [n] = 0 or omitted, filters only "full" rows sum(blnks) = 0
  - [n] > 0 filters the rows that have sum(blnks) <= n
  - [n] < 0 filters only rows that have sum(blnks) >= abs(n)


### Code

{% capture code %}
AHCLEAN = LAMBDA(ar, [n],
    LET(
        a, IF(ISERROR(ar), "", IF(ar = "", "", ar)),
        x, BYROW(a, LAMBDA(a, SUM(--(a = "")))),
        m, MIN(ABS(n), MAX(x)),
        FILTER(a, IF(n >= 0, x <= m, x >= m))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}