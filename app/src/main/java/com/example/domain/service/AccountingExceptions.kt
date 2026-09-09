package com.example.domain.service

sealed class AccountingDomainException(message: String) : Exception(message)

class HierarchyValidationException(message: String) : AccountingDomainException(message)

class VoucherValidationException(message: String) : AccountingDomainException(message)

class PostingException(message: String) : AccountingDomainException(message)
