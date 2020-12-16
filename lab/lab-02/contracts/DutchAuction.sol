pragma solidity ^0.4.24;

import "./Auction.sol";

contract DutchAuction is Auction {
    uint256 public initialPrice;
    uint256 public biddingPeriod;
    uint256 public priceDecrement;

    uint256 internal auctionEnd;
    uint256 internal auctionStart;

    /// Creates the DutchAuction contract.
    ///
    /// @param _sellerAddress Address of the seller.
    /// @param _judgeAddress Address of the judge.
    /// @param _timer Timer reference
    /// @param _initialPrice Start price of dutch auction.
    /// @param _biddingPeriod Number of time units this auction lasts.
    /// @param _priceDecrement Rate at which price is lowered for each time unit
    ///                        following linear decay rule.
    constructor(
        address _sellerAddress,
        address _judgeAddress, // address(0) is there if there no judge
        Timer _timer,
        uint256 _initialPrice,
        uint256 _biddingPeriod,
        uint256 _priceDecrement
    ) public Auction(_sellerAddress, _judgeAddress, _timer) {
        initialPrice = _initialPrice;
        biddingPeriod = _biddingPeriod;
        priceDecrement = _priceDecrement;
        auctionStart = time();
        auctionEnd = auctionStart + _biddingPeriod;
    }

    /// In a Dutch auction, the winner is the first person who bids with
    /// a price higher than the current price.
    /// This method should only be called while the auction is active.
    function bid() public payable {
        uint256 timestamp = time();
        uint256 decrement = (timestamp - auctionStart) * priceDecrement;
        decrement = decrement > initialPrice ? initialPrice : decrement;
        uint256 price = initialPrice - decrement;

        require(
            msg.value >= price,
            "[ReqFail] Can't bid with a bid smaller than the asking price."
        );
        require(
            outcome == Outcome.NOT_FINISHED,
            "[ReqFail] Can't bid on an inactive auction."
        );
        require(
            timestamp < auctionEnd,
            "[ReqFail] Can't bid after bidding period ended."
        );
        require(
            highestBidderAddress == address(0),
            "[ReqFail] Can't bid after a winner has been decided."
        );

        if (msg.value > price) {
            msg.sender.transfer(msg.value - price);
        }

        require(
            address(this).balance >= price,
            "[ReqFail] Can't bid without the money appearing in the contract's balance."
        );

        highestBidderAddress = msg.sender;
    }
}
