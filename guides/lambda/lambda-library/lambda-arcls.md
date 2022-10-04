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

## ARCLS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/arrange.1169370/#post-5956130){:target="_blank"}

### About

Re-arranges columns of an array from left to right in any order. 


#### Inputs:

   - ar - array
   - ns - new sequence order clms (whatever index nr. are left out will be appended at the right end of the array)


### Code

{% capture code %}
ARCLS = LAMBDA(ar, ns,
    LET(
        a, IF(ar = "", "", ar),
        s, SEQUENCE(, COLUMNS(a)),
        h, CHOOSECOLS(s, ns),
        x, XMATCH(s, h),
        f, FILTER(s, ISNA(x)),
        INDEX(a, SEQUENCE(ROWS(a)), HSTACK(h, f))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}