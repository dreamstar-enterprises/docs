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

## ARRWS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/arrange.1169370/#post-5956130){:target="_blank"}

### About

Re-arranges rows of an array from left to right in any order.

#### Inputs:

   - ar - array
   - ns - new sequence order rows (whatever index nr. are left out will be appended at the bottom of the array)

### Code

{% capture code %}
ARRWS = LAMBDA(ar, ns,
    LET(
        a, IF(ar = "", "", ar),
        s, SEQUENCE(ROWS(a)),
        h, CHOOSEROWS(s, ns),
        x, XMATCH(s, h),
        f, FILTER(s, ISNA(x)),
        INDEX(a, VSTACK(h, f), SEQUENCE(, COLUMNS(a)))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}