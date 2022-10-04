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

## ASTREAK

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/streak.1217717/){:target="_blank"}

### About

Largest consecutive distribution of a given value, BYROW or BYCOL.

#### Inputs:

  - a - array
  -  v - value
  - [o] - orientation: if omitted or 0, byrow (clm vector result), if 1 , bycol (row vector result)
  - [m] - max argument: if omitted => entire vector, if 1 max(of result vector)

### Code

{% capture code %}
ASTREAK = LAMBDA(a, v, [o], [m],
    LET(
        b, IF(a = v, 1, 0),
        s, LAMBDA(x,
            MAX(SCAN(0, x, LAMBDA(v, i, IF(i, v + i, 0))))
        ),
        i, IF(
            o,
            BYCOL(b, LAMBDA(x, s(x))),
            BYROW(b, LAMBDA(x, s(x)))
        ),
        IF(m, MAX(i), i)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}