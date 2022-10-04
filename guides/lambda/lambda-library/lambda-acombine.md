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

## ACOMBINE

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/acombine.1170731/){:target="_blank"}

### About

Lists all possible combinations between all unique elements found on each column of an array.

In other words - produces a cartesian product!

Calls [AUNIQUE](../lambda-library/lambda-aunique.html), [ACLEAN](../lambda-library/lambda-aclean.html), and [AXLOOKUP](../lambda-library/lambda-axlookup.html).

#### Inputs:

  - a - array
  - cl - array of column indexes, if ignored, entire array will be considered, ex: {1,3} or {2,1} or {3,1,2}

### Code

{% capture code %}
ACOMBINE = LAMBDA(a, cl,
    LET(
        y, INDEX(a, SEQUENCE(ROWS(a)), IF(AND(cl), cl, SEQUENCE(, COLUMNS(a)))),
        u, AUNIQUE(y, -1),
        v, TRANSPOSE(AUNQSRT(TRANSPOSE(u), 1)),
        r, ROWS(v),
        c, COLUMNS(v),
        s, SEQUENCE(, c),
        x, MOD(ROUNDUP(SEQUENCE(r ^ c) / r ^ ABS(s - c), 0) - 1, r) * c + s,
        ACLEAN(AXLOOKUP(x, IF(v = "", 0, SEQUENCE(r, c)), v, , , ), 1)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}