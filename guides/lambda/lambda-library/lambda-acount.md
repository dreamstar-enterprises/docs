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

## ACOUNT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/acount.1184150/){:target="_blank"}

### About

Replaces functionality of: COUNTIF(S)(range,range), COUNTIF(S) with expandable ranges for occurrence counting and COUNTIF(S)(range,unique(range)) for unique total counts. 

Calls [AFLAT](../lambda-library/lambda-aflat.html), & [ARESIZE](../lambda-library/lambda-aresize.html).

#### Inputs:

  - a - array
  - [ct] - count type argument: 0 or omitted, self-count; 1, occurrence self-count; 2, unique count
  - [pu] - print unique argument: 0 or omitted, unique count; 1 or <> 0, array {unique values, unique count}

#### More Info:

*NOTE*: [pu] will only work for when [ct] is '2'.

### Code

{% capture code %}
ACOUNT = LAMBDA(a, [ct], [pu],
    LET(
        r, ROWS(a),
        f, AFLAT(a, 1),
        w, ROWS(f),
        q, UNIQUE(AFLAT(a)),
        u, IF(ct = 2, q, f),
        p, MAP(u, LAMBDA(x, SUM(--(x = f)))),
        k, MAKEARRAY(w, , LAMBDA(r, i, SUM(--(INDEX(f, SEQUENCE(r)) = INDEX(f, r))))),
        y, SWITCH(ct, 0, ARESIZE(p, r), 1, ARESIZE(k, r), 2, IF(pu, CHOOSE({1, 2}, q, p), p)),
        IF(ISNA(y), "check arg.", IFERROR(IF(ct = 2, y, IF(a = "", "", y)), ""))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}