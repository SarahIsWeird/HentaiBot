package com.sarahisweird.hentaibot.i18n

abstract class Resources {
    abstract val noResults: String
    abstract val picture: String
    abstract val noLikedPicture: String
    abstract val blacklistUpdated: String
    abstract val emptyBlacklist: String
    abstract val listBlacklist: String
    abstract val invalidFirstBlacklistArgument: String
    abstract val addedToLikes: String
    abstract val alreadyLiked: String
    abstract val likeRemoved: String
    abstract val somethingWentWrong: String
    abstract val languageUpdated: String
}

object RGerman : Resources() {
    override val noResults = "Es wurde nix gefunden :("
    override val picture = "Bild"
    override val noLikedPicture = "Du hast noch kein Bild geliked!"
    override val blacklistUpdated = "Deine Blacklist wurde geupdated."
    override val emptyBlacklist = "Deine Blacklist ist leer."
    override val listBlacklist = "Diese Tags sind in deiner Blacklist:"
    override val invalidFirstBlacklistArgument = "Das erste Argument muss entweder " +
            "`add`, `remove` oder `show` sein."
    override val addedToLikes = "Das Bild wurde zu deinen Favoriten hinzugefügt!"
    override val alreadyLiked = "Das Bild ist bereits in deinen Favoriten!"
    override val likeRemoved = "Der Like wurde entfernt."
    override val somethingWentWrong = "Da ist wohl etwas schiefgelaufen."
    override val languageUpdated = "Die Serversprache wurde geupdated."
}

object REnglish : Resources() {
    override val noResults = "Couldn't find anything :("
    override val picture = "Picture"
    override val noLikedPicture = "You haven't liked any picture yet!"
    override val blacklistUpdated = "Your blacklist was updated."
    override val emptyBlacklist = "Your blacklist is empty."
    override val listBlacklist = "Your blacklist contains the following tags:"
    override val invalidFirstBlacklistArgument = "The first argument must be either " +
            "`add`, `remove` or `show`."
    override val addedToLikes = "The picture was added to your favourites!"
    override val alreadyLiked = "The picture's already in your favourites!"
    override val likeRemoved = "Your like was removed."
    override val somethingWentWrong = "Well, something went wrong there."
    override val languageUpdated = "Updated the server language."
}