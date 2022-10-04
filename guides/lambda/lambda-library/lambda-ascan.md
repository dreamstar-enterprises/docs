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

## ASCAN

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/ascan.1183314/){:target="_blank"}

### About

Calculates running totals on arrays or vectors (by row, or by column).

#### Inputs:

  - a - array
  - [d] - direction argument ; 0 or omitted scan by array, >1 by clms, <-1 by rows 

#### More Info:

Can handle blanks, null strings, text, errors, d argument wrong input.

### Code

{% capture code %}
ASCAN = LAMBDA(a, [d],
    LET(
        n, ISNUMBER(a),
        r, IF(n, a, 0),
        o, IF(d, d ^ 0 * SIGN(d), 0),
        y, IF(o = 1, TRANSPOSE(r), r),
        s, SCAN(0, y, LAMBDA(v, a, v + a)),
        x, s - IF(o, INDEX(s, , 1) - INDEX(y, , 1)),
        IF(n, IF(o = 1, TRANSPOSE(x), x), a)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}