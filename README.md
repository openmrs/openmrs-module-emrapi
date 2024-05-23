[![Build Status](https://travis-ci.org/openmrs/openmrs-module-emrapi.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-module-emrapi)

openmrs-module-emrapi
====================

Higher-level APIs to support building EMR functionality in OpenMRS, to supplement the data-level APIs in the OpenMRS core.

# Dependencies

## Required core version

* 2.2.1

## Required modules

* reporting (+ serialization.xstream, htmlwidgets, calculation)
* providermanagement
* metadatamapping
* event
* webservices.rest

## Aware of modules

* fhir2
* metadatasharing

# Configuration

* EmrApiProperties + EmrApiConstants wraps much of the configuration, much of which is in GPs and metadata mappings

# Startup

* Creates a role named "Privilege Level: Full", if it doesn't exist, and ensures it has every privilege that does not start with "App:" or "Task:"
* Creates a role named "Privilege Level: High", if it doesn't exist, and ensures it has every privilege that does not start with "App:" or "Task:" and is not in a list of "unsafe" privileges
* Creates metadata source, metadata mappings, and metadata sets that are required for the configuration
* Creates an "Unknown Provider" provider if it does not exist
* Sets default visit assignment handler via global property
* Sets up the patient viewed event listener subscription
* Ensures the person image folder exists

# API

## account

* AccountDomainWrapper - wrapper for Person + User + Provider with methods for getting and manipulating account-related data
* AccountService - CRUD operations for AccountDomainWrapper and opinionated methods for getting roles and privileges
* AccountValidator - validates an AccountDomainWrapper
* ProviderIdentifierGenerator - interface for generating a provider identifier

## adt

* Provides higher level APIs around Admission, Discharge, and Transfer
* These are based on the "Disposition" configuration in DispositionService / DispositionDescriptor and metadata in EmrApiProperties
* A/D/T based around specific encounter type and optional form
* Provides an implementation of automatically creating visits and assigning encounters to visits with appropriate visit types, if no visit is associated with the encounter
* Provides an implementation of closing "stale" visits and a scheduled task to automate this
* Provides additional service methods to manage visits, ensure active visit, deal with overlapping visits, manage visit locations, enter retrospective visits
* Provides implementations for merging patients and merging visits
* Provides APIs for creating checkin encounters, adt encounters, and identify and get inpatient visits

* (reporting module) definitions and evaluators for getting:
  * AwaitingAdmissionVisitQuery - any Visits that are in the state of "awaiting admission"
  * MostRecentAdmissionRequestVisitDataDefinition - data from a Visit relevant for "admission request"

## concept

* Provides additional service methods for getting and searching for concepts

## db

* Various utility classes for dealing with DB and Hibernate queries and access and DAO implementations in support of other services

## descriptor

* Utilities for working defining concept sets and members and creating and manipulating obs groups

## diagnosis

* Diagnosis and CodedOrFreeTextAnswer wrapper classes to represent a diagnosis as a concept set / obs group 
* Deprecated services and utility classes (is core as of 2.2) related to Diagnosis

## disposition

* Concept Set / Obs Group specification for modeling a disposition
* Configuration relating disposition to Admission / Discharge / Transfer
* Json parsing and handling specification for reading in a dispositionConfig.json that determines how disposition obs behave 
* Services and DispositionAction interface and implementations for performing operations as a result of a disposition

## domainwrapper

* Utility classes for creating and manaing domain wrapper classes

## encounter

* EncounterTransaction that provides a json model object for encounter, obs, order, diagnosis, disposition, providers, and additional extensions and context
* Mappers to convert to/from data and EncounterTransaction / json
* Matcher interface and base implementations to find an existing Encounter given a set of parameters
* Processor interface for reading and saving Encounter and EncounterTransaction
* Service methods to leverage the other entities to enable saving/finding/retrieving encounters based on the EncounterTransaction construct
* Logic for handling evolution of Orders through to OpenMRS 1.9

## event

* Basic interface and implementation for publishing events with the event module whenever a user views a patient
* Event listener on patient view events to track in the user's property of emrapi.lastViewedPatientIds

## exitfromcare

* Service with methods to close/reopen programs, visits, patient death information

## merge

* Interfaces for PatientMergeAction and VisitMergeAction, used within the merge methods in the adt package
* Should really have this and the service methods in the adt package together

## metadata

* Utility classes for mds import specifications, likely no longer used

## patient

* PatientDomainWrapper - convenience methods around Patient and related tables, including primary identifier, telephone number, unknown patient, and test patient 
* EmrPatientProfileService - allows associating a patient with a PersonImage
* EmrPatientService - methods to find patients by visit location or primary identifier
* (reporting module) definitions and evaluators for getting patient Primary Identifier

## person

* Services and convenience entities for saving person images to a configurable directory

## test

* Convenience classes for constructing concepts, concept data types, and obs

## utils

* Utility classes / functions in support of the other packages as needed

## visit

* Service and parameter objects to find visits and return associated EncounterTransactions in that visit
* Service methods to get diagnoses obs and patients with particular diagnoses
* VisitDomainWrapper - convenience wrapper to get info around active / open status, encounters contained, diagnoses, dispositions, ADT status

# condition-list

This provides an implementation of condition list functionality, but condition was added to core in 2.2, so this is likely no longer needed
