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

## AXLOOKUP

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/axlookup.1166170/){:target="_blank"}

### About

XLOOKUP for 2D arrays, carries XLOOKUP arguments except the binary ones of search mode.

Calls [AFLAT](../lambda-library/lambda-aflat.html).


#### Inputs:

  - lv - array, lookup value
  - la - array, lookup array
  - ra - array, return array
  - nf - string, not found value
  - m - integer, match mode: 0 or ignored, exact match,-1 exact match or next smaller item, 1 exact match or next larger item, 2 wild character match
  - s - integer, search mode, 0, ignored or 1 search first to last, -1 search last to first


### Code

{% capture code %}
AXLOOKUP = LAMBDA(lv, la, ra, nf, m, s,
    LET(
        x, AND(OR(m = {0, -1, 1, 2}), OR(s = {0, 1, -1})),
        sm, IF(s = 0, 1, s),
        fla, AFLAT(la, 1),
        fra, AFLAT(ra, 1),
        IF(x, XLOOKUP(lv, fla, fra, nf, m, sm), "check values")
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}